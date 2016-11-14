package me.raska.opendvs.worker.probe;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.raska.opendvs.base.model.artifact.Artifact;
import me.raska.opendvs.base.model.artifact.ArtifactComponent;
import me.raska.opendvs.base.model.probe.ProbeActionStep;
import me.raska.opendvs.base.probe.NativeProbe;
import me.raska.opendvs.base.probe.ProbingContext;
import me.raska.opendvs.base.util.Util;

public class MavenProbe implements NativeProbe {
    private static final Logger logger = LoggerFactory.getLogger(MavenProbe.class);

    @Override
    public List<ProbeActionStep> extract(Artifact artifact, List<ArtifactComponent> extractedComponents,
            ProbingContext context) {
        return Arrays.asList();
    }

    private boolean pomMatches(File f) {
        return "pom.xml".equals(f.getName());
    }

    private Model getEffectivePomModel(File pom) throws IOException, XmlPullParserException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileReader(pom));
        // TODO: find out how to get ModelResolver instance to build effective
        // POM
        return model;
    }

    private List<ArtifactComponent> getMavenComponents(File f, ArtifactComponent parentComponent)
            throws IOException, XmlPullParserException {
        List<ArtifactComponent> components = new ArrayList<>();

        Model model = getEffectivePomModel(f);

        ArtifactComponent parentc = new ArtifactComponent();
        parentc.setGroup("maven");
        parentc.setVersion(model.getVersion());
        parentc.setName(model.getGroupId() + ":" + model.getArtifactId());
        parentc.setUid("maven:" + parentc.getName() + ":" + parentc.getVersion());
        parentc.setParentUid((parentComponent != null) ? parentComponent.getUid() : null);
        parentc.setScope("runtime");
        components.add(parentc);

        Map<String, String> properties = mapMavenProperties(model);
        for (Dependency dep : model.getDependencies()) {
            ArtifactComponent c = new ArtifactComponent();
            c.setScope((dep.getScope() == null) ? "runtime" : dep.getScope());
            c.setGroup("maven");

            // replace all properties with this nasty hack
            // TODO: find proper way how to obtain pom model with resolved
            // variables 
            String version = dep.getVersion();
            if (version != null) {
                for (Entry<String, String> entry : properties.entrySet()) {
                    version = version.replace(entry.getKey(), entry.getValue());
                }
            }
            c.setVersion(version);
            c.setName(dep.getGroupId() + ":" + dep.getArtifactId());
            c.setUid("maven:" + c.getName() + ":" + c.getVersion());
            c.setParentUid(parentc.getUid());
            components.add(c);
        }

        return components;
    }

    private Map<String, String> mapMavenProperties(Model model) {
        Map<String, String> map = new HashMap<>();
        model.getProperties().forEach((k, v) -> {
            map.put(String.format("${%s}", k), v.toString());
        });
        if (model.getVersion() != null) {
            map.put("${project.version}", model.getVersion());
        }
        return map;
    }

    private ProbeActionStep detectComponentInResource(File file, ArtifactComponent parentComponent,
            ProbingContext context) {
        ProbeActionStep step = new ProbeActionStep();
        step.setType(ProbeActionStep.Type.DETECTION);
        step.setDetectedComponents(new HashSet<>());
        step.setState(ProbeActionStep.State.SUCCESS);

        StringBuilder sb = new StringBuilder();
        if (parentComponent == null) {
            sb.append("Detecting provided artifact");
        } else {
            sb.append("Detecting extracted component ");
            sb.append(parentComponent.getUid());
            sb.append(" with path ");
            sb.append(context.getStrippedFilePath(file));
        }
        sb.append(System.lineSeparator());

        if (file.isFile()) {
            if (pomMatches(file)) {
                try {
                    List<ArtifactComponent> components;
                    components = getMavenComponents(file, parentComponent);

                    sb.append("Processed " + context.getStrippedFilePath(file) + " with " + components.size()
                            + " runtime dependencies");
                    sb.append(System.lineSeparator());
                    step.getDetectedComponents().addAll(components);
                } catch (Exception e) {
                    step.setState(ProbeActionStep.State.FAILURE);
                    sb.append("Exception " + e.getMessage());
                    sb.append(System.lineSeparator());

                    logger.warn("Obtained Exception while processing Maven file " + file, e);
                }
            }
        } else if (file.isDirectory()) {
            try {
                Files.walk(file.toPath()).parallel().filter(p -> pomMatches(p.toFile())).forEach(p -> {
                    try {
                        File f = p.toFile();
                        List<ArtifactComponent> components = getMavenComponents(f, parentComponent);
                        sb.append("Processed " + context.getStrippedFilePath(f) + " with " + components.size()
                                + " runtime dependencies");
                        sb.append(System.lineSeparator());
                        step.getDetectedComponents().addAll(components);
                    } catch (Exception e) {
                        step.setState(ProbeActionStep.State.FAILURE);
                        sb.append("Exception " + e.getMessage());
                        sb.append(System.lineSeparator());

                        logger.warn("Obtained Exception while processing Maven file " + p, e);
                    }
                });
            } catch (IOException e) {
                step.setState(ProbeActionStep.State.FAILURE);
                sb.append("IOException " + e.getMessage());
                sb.append(System.lineSeparator());

                logger.error("Obtained IOException while processing file " + file, e);
            }
        } else {
            logger.warn("Required resource doesnt exist: {}", file);
            sb.append("Required resource doesn't exist!");
        }

        sb.append("Detected ");
        sb.append(step.getDetectedComponents().size());
        sb.append(" components");
        step.setOutput(sb.toString());
        return step;
    }

    @Override
    public List<ProbeActionStep> detectComponents(Artifact artifact, List<ArtifactComponent> extractedComponents,
            ProbingContext context) {
        Set<ProbeActionStep> steps = new HashSet<>();
        if (extractedComponents.isEmpty()) {
            // initial parse
            steps.add(detectComponentInResource(context.getArtifactFile(), null, context));
        } else {
            for (ArtifactComponent component : extractedComponents) {
                try {
                    steps.add(detectComponentInResource(context.getComponentDirectoryPath(component).toFile(),
                            component, context));
                } catch (IOException e) {
                    steps.add(Util.generateErrorStep("IOException while processing " + component.getUid()));
                    logger.error("Obtained IOException while processing component " + component.getUid()
                            + " for artifact " + artifact.getId(), e);
                }
            }
        }
        return new ArrayList<>(steps);
    }

}
