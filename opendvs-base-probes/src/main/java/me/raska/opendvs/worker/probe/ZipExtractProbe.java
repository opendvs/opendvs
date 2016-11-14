package me.raska.opendvs.worker.probe;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.raska.opendvs.base.model.artifact.Artifact;
import me.raska.opendvs.base.model.artifact.ArtifactComponent;
import me.raska.opendvs.base.model.probe.ProbeActionStep;
import me.raska.opendvs.base.probe.NativeProbe;
import me.raska.opendvs.base.probe.ProbingContext;
import me.raska.opendvs.base.util.Util;

public class ZipExtractProbe implements NativeProbe {
    private static final Logger logger = LoggerFactory.getLogger(ZipExtractProbe.class);
    private static final List<String> EXTRACT_MIME = Collections.unmodifiableList(Arrays.asList(
            "application/x-java-archive", "application/x-webarchive", "application/java-archive", "application/zip"));

    @Override
    public List<ProbeActionStep> extract(Artifact artifact, List<ArtifactComponent> extractedComponents,
            ProbingContext context) {
        Set<ProbeActionStep> steps = new HashSet<>();
        if (extractedComponents.isEmpty()) {
            // initial parse
            steps.add(extractResource(context.getArtifactFile(), null, context));
        } else {
            for (ArtifactComponent component : extractedComponents) {
                try {
                    steps.add(
                            extractResource(context.getComponentDirectoryPath(component).toFile(), component, context));
                } catch (IOException e) {
                    steps.add(Util.generateErrorStep(
                            "Obtained IOException while processing component " + component.getUid()));
                    logger.warn("Obtained IOException while processing component " + component.getUid(), e);
                }
            }
        }
        // if nothing was found
        steps.remove(null);
        return new ArrayList<>(steps);
    }

    private ProbeActionStep extractResource(File file, ArtifactComponent parent, ProbingContext context) {
        ProbeActionStep step = new ProbeActionStep();
        step.setType(ProbeActionStep.Type.EXTRACTION);
        step.setDetectedComponents(new HashSet<>());
        step.setState(ProbeActionStep.State.SUCCESS);

        StringBuilder sb = new StringBuilder();
        if (parent == null) {
            sb.append("Processing provided artifact");
        } else {
            sb.append("Processing extracted component ");
            sb.append(parent.getUid());
            sb.append(" with path ");
            sb.append(context.getStrippedFilePath(file));
        }
        sb.append(System.lineSeparator());

        if (file.isFile()) {
            try {
                if (EXTRACT_MIME.contains(Files.probeContentType(file.toPath()))) {
                    ArtifactComponent component = new ArtifactComponent();
                    component.setUid("zip:" + file.getName());
                    component.setName(file.getName());

                    if (parent != null) {
                        component.setParentUid(parent.getUid());
                    }
                    component.setHash(Util.getFileSha1Checksum(file));
                    component.setGroup("zip");

                    File f = context.getComponentDirectoryPath(component).toFile();
                    if (f.exists()) {
                        throw new IOException("Directory already exists" + f.getAbsolutePath());
                    }
                    Util.unzip(file, f);
                    step.getDetectedComponents().add(component);
                }
            } catch (IOException e) {
                step.setState(ProbeActionStep.State.FAILURE);
                sb.append("Exception " + e.getMessage());
                sb.append(System.lineSeparator());

                logger.warn("Obtained exception while extracting zip file " + file, e);
            }
        } else if (file.isDirectory()) {
            try {
                Files.walk(file.toPath()).parallel().filter(p -> {
                    try {
                        return EXTRACT_MIME.contains(Files.probeContentType(p));
                    } catch (Exception e) {
                        return false;
                    }
                }).forEach(p -> {
                    try {
                        File componentFile = p.toFile();
                        ArtifactComponent component = new ArtifactComponent();
                        component.setUid("zip:" + componentFile.getName());
                        component.setName(componentFile.getName());

                        if (parent != null) {
                            component.setParentUid(parent.getUid());
                        }
                        component.setHash(Util.getFileSha1Checksum(componentFile));
                        component.setGroup("zip");

                        File f = context.getComponentDirectoryPath(component).toFile();
                        if (f.exists()) {
                            throw new IOException("Directory already exists" + f.getAbsolutePath());
                        }
                        Util.unzip(componentFile, f);
                        step.getDetectedComponents().add(component);
                    } catch (Exception e) {
                        step.setState(ProbeActionStep.State.FAILURE);
                        sb.append("Exception " + e.getMessage());
                        sb.append(System.lineSeparator());

                        logger.warn("Obtained exception while extracting zip file " + p, e);
                    }
                });
            } catch (IOException e) {
                step.setState(ProbeActionStep.State.FAILURE);
                sb.append("Exception " + e.getMessage());
                sb.append(System.lineSeparator());

                logger.warn("Obtained exception while walking path " + file, e);
            }
        } else {
            step.setState(ProbeActionStep.State.FAILURE);
            sb.append("Required resource doesn't exist");
            sb.append(System.lineSeparator());

            logger.warn("Required resource doesnt exist: " + file);
        }

        sb.append("Extracted ");
        sb.append(step.getDetectedComponents().size());
        sb.append(" components");
        step.setOutput(sb.toString());
        return step;
    }

    @Override
    public List<ProbeActionStep> detectComponents(Artifact artifact, List<ArtifactComponent> extractedComponents,
            ProbingContext context) {
        return Arrays.asList();
    }

}
