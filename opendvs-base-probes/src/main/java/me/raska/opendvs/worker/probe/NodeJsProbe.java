package me.raska.opendvs.worker.probe;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.raska.opendvs.base.model.artifact.Artifact;
import me.raska.opendvs.base.model.artifact.ArtifactComponent;
import me.raska.opendvs.base.model.probe.ProbeActionStep;
import me.raska.opendvs.base.probe.NativeProbe;
import me.raska.opendvs.base.probe.ProbingContext;
import me.raska.opendvs.base.util.Util;
import me.raska.opendvs.worker.probe.npm.NodePackage;

public class NodeJsProbe implements NativeProbe {
    private static final Logger logger = LoggerFactory.getLogger(NodeJsProbe.class);

    private final ObjectMapper jsonMapper;

    public NodeJsProbe() {
        this.jsonMapper = new ObjectMapper();
    }

    @Override
    public List<ProbeActionStep> extract(Artifact artifact, List<ArtifactComponent> extractedComponents,
            ProbingContext context) {
        return Arrays.asList();
    }

    private boolean packageMatches(File f) {
        return "package.json".equals(f.getName());
    }

    private List<ArtifactComponent> getNodeComponents(File f, ArtifactComponent parentComponent)
            throws IOException, XmlPullParserException {
        final List<ArtifactComponent> components = new ArrayList<>();

        // TODO: Build full dependency tree, e.g. via https://github.com/pahen/madge
        final NodePackage pkg = jsonMapper.readValue(f, NodePackage.class);
        final ArtifactComponent parentc = new ArtifactComponent();
        parentc.setGroup("npm");
        parentc.setVersion(pkg.getVersion());
        parentc.setName(pkg.getName());
        parentc.setUid("npm:" + parentc.getName() + ":" + parentc.getVersion());
        parentc.setParentUid((parentComponent != null) ? parentComponent.getUid() : null);
        parentc.setScope("runtime");
        components.add(parentc);


        if (pkg.getDependencies() != null) {
            for (Entry<String, String> entry: pkg.getDependencies().entrySet()) {
                ArtifactComponent c = new ArtifactComponent();
                c.setScope("runtime"); // this is always runtime
                c.setGroup("npm");
                c.setVersion(entry.getValue());
                c.setName(entry.getKey());
                c.setUid("npm:"+c.getName() + ":" + c.getVersion());
                c.setParentUid(parentc.getUid());
                components.add(c);
            }
        }

        if (pkg.getDevDependencies() != null) {
            for (Entry<String, String> entry: pkg.getDevDependencies().entrySet()) {
                ArtifactComponent c = new ArtifactComponent();
                c.setScope("test"); // TODO: validate if we want to use test instead of devel
                c.setGroup("npm");
                c.setVersion(entry.getValue());
                c.setName(entry.getKey());
                c.setUid("npm:"+c.getName() + ":" + c.getVersion());
                c.setParentUid(parentc.getUid());
                components.add(c);
            }
        }

        return components;
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
            if (packageMatches(file)) {
                try {
                    List<ArtifactComponent> components;
                    components = getNodeComponents(file, parentComponent);

                    sb.append("Processed " + context.getStrippedFilePath(file) + " with " + components.size()
                            + " runtime dependencies");
                    sb.append(System.lineSeparator());
                    step.getDetectedComponents().addAll(components);
                } catch (Exception e) {
                    step.setState(ProbeActionStep.State.FAILURE);
                    sb.append("Exception " + e.getMessage());
                    sb.append(System.lineSeparator());

                    logger.warn("Obtained Exception while processing Node.js file " + file, e);
                }
            }
        } else if (file.isDirectory()) {
            try {
                Files.walk(file.toPath()).parallel().filter(p -> packageMatches(p.toFile())).forEach(p -> {
                    try {
                        File f = p.toFile();
                        List<ArtifactComponent> components = getNodeComponents(f, parentComponent);
                        sb.append("Processed " + context.getStrippedFilePath(f) + " with " + components.size()
                                + " runtime dependencies");
                        sb.append(System.lineSeparator());
                        step.getDetectedComponents().addAll(components);
                    } catch (Exception e) {
                        step.setState(ProbeActionStep.State.FAILURE);
                        sb.append("Exception " + e.getMessage());
                        sb.append(System.lineSeparator());

                        logger.warn("Obtained Exception while processing Node.js file " + p, e);
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
