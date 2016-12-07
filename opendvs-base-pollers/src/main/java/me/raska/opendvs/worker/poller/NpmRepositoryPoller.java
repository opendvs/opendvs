package me.raska.opendvs.worker.poller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.raska.opendvs.base.model.Component;
import me.raska.opendvs.base.model.ComponentVersion;
import me.raska.opendvs.base.model.poller.PollerAction;
import me.raska.opendvs.base.model.poller.PollerActionStep;
import me.raska.opendvs.base.poller.NativePoller;
import me.raska.opendvs.worker.poller.npm.NpmArtifactMetadata;

public class NpmRepositoryPoller implements NativePoller {
    private static final Logger logger = LoggerFactory.getLogger(NpmRepositoryPoller.class);

    private final RestTemplate restTemplate;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");

    // todo: get from context
    private String repo = "http://registry.npmjs.org/";

    public NpmRepositoryPoller() {
        this.restTemplate = new RestTemplateBuilder().defaultMessageConverters()
                .additionalMessageConverters(plainMessageConverter()).build();
    }

    @SuppressWarnings("rawtypes")
    private HttpMessageConverter plainMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        MappingJackson2HttpMessageConverter msgConverter = new MappingJackson2HttpMessageConverter();
        msgConverter.setSupportedMediaTypes(Arrays.asList(MediaType.TEXT_PLAIN));
        msgConverter.setObjectMapper(objectMapper);
        return msgConverter;
    }

    @Override
    public void process(PollerAction action, Consumer<PollerAction> callback) {
        if (action.getFilter() != null) {
            if (action.getFilter().equals("npm")) {
                detectArtifactDir(repo, action, callback);
            } else if (action.getFilter().startsWith("npm:")) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Triggering NPM poller due to filter " + action.getFilter());
                }
                handlePackageMetadata(buildFilterUrl(repo, action.getFilter()), action, callback);
            } else if (logger.isDebugEnabled()) {
                logger.debug("Filter " + action.getFilter() + " is not applicable for NPM poller");
            }
        } else {
            detectArtifactDir(repo, action, callback);
        }
    }

    private String buildFilterUrl(String base, String filter) {
        String[] sp = filter.split(":");
        return base + sp[1];
    }

    private void handlePackageMetadata(String artifactUrl, PollerAction action, Consumer<PollerAction> callback) {
        PollerActionStep step = new PollerActionStep();
        step.setState(PollerActionStep.State.SUCCESS);
        step.setType(PollerActionStep.Type.PACKAGE);
        step.setPoller(getId());
        step.setStarted(new Date());

        StringBuilder sb = new StringBuilder();
        sb.append("Trying to fetch component from url ");
        sb.append(artifactUrl);
        sb.append(System.lineSeparator());

        PollerAction act = new PollerAction();
        act.setId(action.getId());

        try {
            NpmArtifactMetadata metadata = restTemplate.getForObject(artifactUrl, NpmArtifactMetadata.class);
            if (metadata != null) {
                Component c = new Component();
                if (metadata.getTags() != null) {
                    c.setLatestVersion(metadata.getTags().get("latest"));
                }
                c.setGroup("npm");
                c.setName(metadata.getName());
                c.setId("npm:" + c.getName());

                Set<ComponentVersion> versions = new HashSet<>();
                if (metadata.getVersions() != null) {
                    metadata.getVersions().forEach((k, v) -> {
                        ComponentVersion version = new ComponentVersion();
                        version.setHash("sha1:" + v.getShasum());
                        version.setSynced(new Date());

                        if (metadata.getTime() != null) {
                            Object dt = metadata.getTime().get(k);
                            if (dt != null) {
                                try {
                                    version.setPublished(dateFormat.parse(dt.toString()));
                                } catch (ParseException e) {
                                    logger.info("Cannot parse date " + dt + " for artifact " + artifactUrl
                                            + " and version " + k);
                                }
                            }
                        }
                        version.setSource(artifactUrl);
                        version.setVersion(v.getVersion());
                        version.setPackaging("npm");
                        version.setId(c.getId() + ":" + version.getVersion());

                        versions.add(version);
                    });
                } else {
                    // no detected version, scrap the result
                    return;
                }
                c.setVersions(versions);

                Set<Component> set = new HashSet<>();
                set.add(c);

                step.setDetectedComponents(set);
            }
        } catch (Exception e) {
            step.setState(PollerActionStep.State.FAILURE);
            sb.append("Obtained fatal exception " + e.getMessage());

            logger.warn("Cannot obtain components from url " + artifactUrl, e);
        }
        step.setOutput(sb.toString());
        step.setEnded(new Date());
        act.setSteps(Arrays.asList(step));
        callback.accept(act);
    }

    @SuppressWarnings("unchecked")
    private void detectArtifactDir(String url, PollerAction action, Consumer<PollerAction> callback) {
        logger.debug("Fetching NPM index");
        Map<Object, Object> obj = restTemplate.getForObject("http://registry.npmjs.org/-/all", Map.class);
        logger.info("NPM index fetched, total " + (obj.size() - 1) + " entries");

        List<Object> components = obj.values().stream().filter(v -> v instanceof LinkedHashMap)
                .map(v -> ((LinkedHashMap<Object, Object>) v).get("name")).collect(Collectors.toList());

        obj = null; // for GC, it's large
        logger.debug("Parsed to " + components.size() + " artifacts");
        int step = components.size() / 100;
        int counter = 0;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < components.size(); i++) {
            handlePackageMetadata(repo + components.get(i), action, callback);

            counter++;
            if (counter == step) {
                double percent = ((i + 1) * 10000 / components.size()) / 100.0;
                double remaining = (((System.currentTimeMillis() - startTime) / percent) * (100 - percent)) / 60000;
                double average = 1000 / ((System.currentTimeMillis() - startTime) / i);
                logger.info("Fetched " + (i + 1) + "/" + components.size() + " artifacts (" + percent
                        + "% total), remaining " + remaining + " minutes, average processing " + average + "/sec");
                counter = 0;
            }
        }
        logger.info("Fetched all NPM index");
    }

    @Override
    public String getId() {
        return "npm";
    }
}
