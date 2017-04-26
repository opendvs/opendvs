package me.raska.opendvs.worker.poller;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.raska.opendvs.base.model.Component;
import me.raska.opendvs.base.model.ComponentVersion;
import me.raska.opendvs.base.model.poller.PollerAction;
import me.raska.opendvs.base.model.poller.PollerActionStep;
import me.raska.opendvs.base.poller.NativePoller;

public class MavenRepositoryPoller implements NativePoller {
    private static final Logger logger = LoggerFactory.getLogger(MavenRepositoryPoller.class);

    private static final String[] MAVEN_PACKAGES = { "jar", "war", "ear", "ejb", "pom", "zip" };

    // todo: get from context
    private String repo = "https://repo1.maven.org/maven2/";
    private XPathExpression artifactIdXPath;
    private XPathExpression groupIdXPath;;
    private XPathExpression latestVersionXPath;
    private XPathExpression lastUpdateXPath;

    public MavenRepositoryPoller() throws Exception {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        artifactIdXPath = xpath.compile("/metadata/artifactId/text()");
        groupIdXPath = xpath.compile("/metadata/groupId/text()");
        latestVersionXPath = xpath.compile("/metadata/versioning/latest/text()");
        lastUpdateXPath = xpath.compile("/metadata/versioning/lastUpdated/text()");
    }

    @Override
    public void process(PollerAction action, Consumer<PollerAction> callback) {
        if (action.getFilter() != null) {
            if (action.getFilter().startsWith("maven:")) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Triggering Maven poller due to filter " + action.getFilter());
                }
                detectArtifactDir(buildFilterUrl(repo, action.getFilter()), action, callback);
            } else if (logger.isDebugEnabled()) {
                logger.debug("Filter " + action.getFilter() + " is not applicable for Maven poller");
            }
        } else {
            detectArtifactDir(repo, action, callback);
        }
    }

    String buildFilterUrl(String base, String filter) {
        String[] sp = filter.split(":");
        StringBuilder sb = new StringBuilder();
        sb.append(base);
        sb.append(sp[1].replace(".", "/"));

        if (sp.length > 2) {
            sb.append("/");
            sb.append(sp[2]); // don't replace dots in artifactId
            sb.append("/");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Generated filter URL " + sb.toString() + " for filter " + filter);
        }
        return sb.toString();
    }

    private void handlePackageMetadata(String baseUrl, Elements links, PollerAction action,
            Consumer<PollerAction> callback) {
        PollerActionStep step = new PollerActionStep();
        step.setState(PollerActionStep.State.SUCCESS);
        step.setType(PollerActionStep.Type.PACKAGE);
        step.setPoller(getId());
        step.setStarted(new Date());

        StringBuilder sb = new StringBuilder();
        sb.append("Trying to fetch component from url ");
        sb.append(baseUrl);
        sb.append(System.lineSeparator());

        PollerAction act = new PollerAction();
        act.setId(action.getId());

        try {
            // TODO: figure out if it's really thread-unsafe - 'FWK005 parse may not be called while parsing.'
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = docBuilder.parse(new URL(baseUrl + "maven-metadata.xml").openStream());
            String artId = (String) artifactIdXPath.evaluate(doc, XPathConstants.STRING);
            String groupId = (String) groupIdXPath.evaluate(doc, XPathConstants.STRING);

            if (artId == null || groupId == null || artId.isEmpty() || groupId.isEmpty()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Obtained invalid package metadata at url " + baseUrl);
                }
                return; // don't handle invalid packages
            }

            String latestVersion = (String) latestVersionXPath.evaluate(doc, XPathConstants.STRING);
            String lastUpdate = (String) lastUpdateXPath.evaluate(doc, XPathConstants.STRING);
            // TODO: caching based on this + hash + lastModified field

            Component c = new Component();
            c.setGroup("maven");
            c.setName(groupId + ":" + artId);
            c.setId("maven:" + c.getName());
            c.setVersions(new HashSet<>());

            sb.append("Obtained metadata for ");
            sb.append(c.getId());
            sb.append(System.lineSeparator());

            // TODO: diffs

            for (Element e : links) {
                String link = e.text();
                if (link == null || link.isEmpty() || !link.endsWith("/") || link.startsWith(".")) {
                    continue; // handle only folders
                }

                String version = link.substring(0, link.length() - 1);
                ComponentVersion cv = buildComponentVersion(baseUrl, artId, version);
                if (cv != null) {
                    cv.setId(c.getId() + ":" + cv.getVersion());
                    c.getVersions().add(cv);
                    sb.append("Detected component version ");
                    sb.append(cv.getVersion());
                    sb.append(" with hash ");
                    sb.append(cv.getHash());
                    sb.append(" and packaging ");
                    sb.append(cv.getPackaging());
                    sb.append(", last modified on ");
                    sb.append(cv.getPublished());
                    sb.append(System.lineSeparator());

                } else {
                    sb.append("Could not detect component version " + version);
                    sb.append(System.lineSeparator());
                    step.setState(PollerActionStep.State.FAILURE);
                }
            }

            if (latestVersion == null || latestVersion.isEmpty()) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Latest version for component " + c.getId() + " is empty, determining from version list");
                }

                Optional<ComponentVersion> v = c.getVersions().stream()
                        .max((a, b) -> a.getPublished().compareTo(b.getPublished()));
                if (v.isPresent() && v.get().getPublished().getTime() > 0) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Determined latest version for component " + c.getId() + " with value "
                                + v.get().getVersion() + " via published field");
                    }
                    latestVersion = v.get().getVersion();
                } else {
                    v = c.getVersions().stream().max((a, b) -> a.getVersion().compareTo(b.getVersion()));
                    if (v.isPresent()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Determined latest version for component " + c.getId() + " with value "
                                    + v.get().getVersion() + " via string comparsion");
                        }
                        latestVersion = v.get().getVersion();
                    } else if (logger.isDebugEnabled()) {
                        logger.debug("Cannot determine latest version for component " + c.getId()
                                + ", ensure this component has any versions (detected " + c.getVersions().size() + ")");
                    }
                }
            }
            c.setLatestVersion(latestVersion);

            Set<Component> set = new HashSet<>();
            set.add(c);

            step.setDetectedComponents(set);
        } catch (Exception e) {
            step.setState(PollerActionStep.State.FAILURE);
            sb.append("Obtained fatal exception " + e.getMessage());

            logger.warn("Cannot obtain components from url " + baseUrl, e);
        }
        step.setOutput(sb.toString());
        step.setEnded(new Date());
        act.setSteps(Arrays.asList(step));
        callback.accept(act);
    }

    private ComponentVersion buildComponentVersion(String baseUrl, String artifactId, String version) {
        ComponentVersion ver = new ComponentVersion();
        ver.setSynced(new Date());

        String partial = baseUrl + version + "/" + artifactId + "-" + version + ".";
        for (String pkg : MAVEN_PACKAGES) {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL(partial + pkg).openConnection();
                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    long lastModified = con.getLastModified();
                    ver.setPublished(new Date(lastModified));
                    ver.setVersion(version);
                    ver.setPackaging(pkg);
                    ver.setSource(baseUrl); // TODO e.g. maven central
                    try {
                        ver.setHash(
                                "sha1:" + IOUtils.toString(new URL(partial + pkg + ".sha1"), Charset.defaultCharset()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return ver;
                }
            } catch (Exception e) {
                logger.warn("Obtained exception while determining artifact type " + pkg + " for " + artifactId
                        + " from " + baseUrl, e);
            }
        }

        return null;
    }

    private void detectArtifactDir(String url, PollerAction action, Consumer<PollerAction> callback) {
        try {
            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("a[href]");

            /* first detect if versions should be parsed */
            for (Element e : links) {
                if ("maven-metadata.xml".equals(e.text())) {
                    handlePackageMetadata(url, links, action, callback);
                    return; // don't parse anything else
                }
            }

            for (Element e : links) {
                String fragment = e.text();
                if (isLocalDir(fragment)) {
                    // System.out.println(url + " => " + fragment);
                    detectArtifactDir(url + fragment, action, callback);
                }
            }
        } catch (Exception e) {
            logger.warn("Obtained exeption when finding subdirectories for " + url, e);
        }
    }

    private boolean isLocalDir(String fragment) {
        return fragment != null && fragment.endsWith("/") && !fragment.startsWith(".")
                && fragment.split("/").length == 1;
    }

    @Override
    public String getId() {
        return "maven";
    }

}
