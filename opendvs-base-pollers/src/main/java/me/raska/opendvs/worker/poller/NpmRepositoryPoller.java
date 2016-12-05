package me.raska.opendvs.worker.poller;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import me.raska.opendvs.base.model.Component;
import me.raska.opendvs.base.model.ComponentVersion;
import me.raska.opendvs.base.model.poller.PollerAction;
import me.raska.opendvs.base.model.poller.PollerActionStep;
import me.raska.opendvs.base.poller.NativePoller;
import me.raska.opendvs.worker.poller.npm.NpmArtifactMetadata;

public class NpmRepositoryPoller implements NativePoller {
    private static final Logger logger = LoggerFactory.getLogger(NpmRepositoryPoller.class);

    private final RestTemplate restTemplate;

    // todo: get from context
    private String repo = "http://registry.npmjs.org/";

    public NpmRepositoryPoller() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void process(PollerAction action, Consumer<PollerAction> callback) {
        if (action.getFilter() != null) {
            if (action.getFilter().startsWith("npm:")) {
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
                c.setLatestVersion(metadata.getTags().get("latest"));
                c.setGroup("npm");
                c.setName(metadata.getName());
                c.setId("npm:" + c.getName());

                Set<ComponentVersion> versions = new HashSet<>();
                metadata.getVersions().forEach((k, v) -> {
                    ComponentVersion version = new ComponentVersion();
                    version.setHash("sha1:" + v.getShasum());
                    version.setSynced(new Date());
                    version.setPublished(metadata.getTime().get(k));
                    version.setSource(artifactUrl);
                    version.setVersion(v.getVersion());
                    version.setPackaging("npm");
                    version.setId(c.getId() + ":" + version.getVersion());

                    versions.add(version);
                });

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

    private void detectArtifactDir(String url, PollerAction action, Consumer<PollerAction> callback) {
        // TODO: Load artifact data from http://registry.npmjs.org/-/all , it
        // sadly doesn't contain list of all versions and its update dates, so
        // another request for each artifact is necessary
    }

    @Override
    public String getId() {
        return "npm";
    }
}
