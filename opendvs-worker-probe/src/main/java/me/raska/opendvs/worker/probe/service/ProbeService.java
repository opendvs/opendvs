package me.raska.opendvs.worker.probe.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import me.raska.opendvs.base.artifact.ArtifactSource;
import me.raska.opendvs.base.model.artifact.Artifact;
import me.raska.opendvs.base.model.artifact.ArtifactComponent;
import me.raska.opendvs.base.model.probe.ProbeAction;
import me.raska.opendvs.base.model.probe.ProbeActionStep;
import me.raska.opendvs.base.probe.NativeProbe;
import me.raska.opendvs.base.probe.ProbingContext;
import me.raska.opendvs.base.util.Util;
import me.raska.opendvs.worker.probe.exception.UnsatisfiedDependencyException;

@Service
public class ProbeService {
    private static final Logger logger = LoggerFactory.getLogger(ProbeService.class);

    private Set<NativeProbe> nativeProbes;
    private Map<String, ArtifactSource> artifactSources;

    public void scanArtifact(ProbeAction action) {
        assert action != null : "action is null";
        assert action.getSteps() != null : "action steps are null";

        action.setStarted(new Date());
        action.setSteps(new ArrayList<>());
        Artifact art = action.getArtifact();

        List<ArtifactComponent> extractedComponents = new ArrayList<>();
        long counter = 0;

        ProbingContext context = new ProbingContext();

        logger.info("Started processing of action " + action.getId());

        try {
            context.setArtifactFile(fetchArtifact(art));
        } catch (Exception e) {
            logger.warn("Cannot download artifact " + art.getId(), e);
            action.getSteps().add(Util.generateErrorStep(e.getMessage()));
        }

        if (context.getArtifactFile() != null) {
            do {
                // Loop protection
                if (counter > action.getMaxIterations()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Forcely ended detection in action " + action.getId()
                                + " due to reached threshold of maximum iterations");
                    }
                    action.getSteps().add(Util.generateErrorStep(
                            "Reached maximum " + action.getMaxIterations() + " iterations, stopping!"));
                    break;
                }

                List<ArtifactComponent> contextExtracted = new ArrayList<>(extractedComponents);
                extractedComponents.clear();

                if (logger.isDebugEnabled()) {
                    logger.debug("Trying to detect and extract components in action " + action.getId()
                            + ", previous iteration extracted " + contextExtracted.size() + " components");
                }
                for (NativeProbe probe : nativeProbes) {

                    try {
                        List<ProbeActionStep> steps = probe.detectComponents(art, contextExtracted, context);
                        action.getSteps().addAll(steps);

                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "Detection probe " + probe + " generated " + steps.size() + " steps and detected "
                                            + steps.stream().flatMap(s -> s.getDetectedComponents().stream())
                                                    .collect(Collectors.toList()).size()
                                            + " components");
                        }
                    } catch (Exception e) {
                        String msg = "Detection probe " + probe.getClass() + " threw unhandled exception for action "
                                + action.getId();
                        logger.error(msg, e);
                        action.getSteps().add(Util.generateErrorStep(msg));
                    }

                    try {
                        List<ProbeActionStep> steps = probe.extract(art, contextExtracted, context);
                        steps.stream().forEach(s -> extractedComponents.addAll(s.getDetectedComponents()));
                        action.getSteps().addAll(steps);

                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "Extraction probe " + probe + " generated " + steps.size() + " steps and extracted "
                                            + steps.stream().flatMap(s -> s.getDetectedComponents().stream())
                                                    .collect(Collectors.toList()).size()
                                            + " components");
                        }
                    } catch (Exception e) {
                        String msg = "Extraction probe " + probe.getClass() + " threw unhandled exception for action "
                                + action.getId();
                        logger.error(msg, e);
                        action.getSteps().add(Util.generateErrorStep(msg));
                    }

                }

                counter++;
            } while (!extractedComponents.isEmpty());
        }

        // cleanup
        try {
            context.cleanup();
        } catch (IOException e) {
            logger.error("Cannot cleanup scanning context for action " + action.getId(), e);
        }

        try {
            cleanupArtifact(art, context);
        } catch (Exception e) {
            logger.warn("Cannot download artifact " + art.getId(), e);
            action.getSteps().add(Util.generateErrorStep(e.getMessage()));
        }

        action.setEnded(new Date());
        action.setState(ProbeAction.State.SUCCESS);
        // can be faster than stream since we only care about first false
        for (ProbeActionStep step : action.getSteps()) {
            if (step.getState() == ProbeActionStep.State.FAILURE) {
                action.setState(ProbeAction.State.FAILURE);
                if (logger.isDebugEnabled()) {
                    logger.debug("Setting action " + action.getId() + " state to FAILURE due to step '"
                            + step.getOutput() + "' being failure");
                }
            }
        }

        logger.info("Ended processing of action " + action.getId());
    }

    private File fetchArtifact(Artifact art) throws Exception {
        if (!artifactSources.containsKey(art.getSourceType())) {
            throw new UnsatisfiedDependencyException(
                    "Artifact type " + art.getSourceType() + " not found for artifact " + art.getId());
        }

        return artifactSources.get(art.getSourceType()).getResource(art);
    }

    private void cleanupArtifact(Artifact art, ProbingContext context) throws Exception {
        if (!artifactSources.containsKey(art.getSourceType())) {
            throw new UnsatisfiedDependencyException(
                    "Artifact type " + art.getSourceType() + " not found for artifact " + art.getId());
        }

        if (context.getArtifactFile() == null) {
            // silently ignore
            return;
        }

        artifactSources.get(art.getSourceType()).cleanupResource(context.getArtifactFile());
    }

    @PostConstruct
    private void init() {
        Reflections ref = new Reflections(ClasspathHelper.forClassLoader(ClasspathHelper.contextClassLoader(),
                ClasspathHelper.staticClassLoader(), ClassLoader.getSystemClassLoader()));
        initNativeProbes(ref);
        initArtifactSources(ref);
    }

    private void initNativeProbes(Reflections ref) {
        nativeProbes = new LinkedHashSet<>();

        for (Class<? extends NativeProbe> probe : ref.getSubTypesOf(NativeProbe.class)) {
            try {
                NativeProbe p = probe.newInstance();
                nativeProbes.add(p);

                if (logger.isDebugEnabled()) {
                    logger.debug("Instantiated native probe " + probe);
                }

            } catch (InstantiationException | IllegalAccessException e) {
                logger.warn("Cannot instantiate native probe " + probe, e);
            }
        }

        if (nativeProbes.isEmpty()) {
            throw new UnsatisfiedDependencyException("Cannot detect any native probes!");
        }

        logger.info("Instantiated {} native probes ", nativeProbes.size());
    }

    private void initArtifactSources(Reflections ref) {
        artifactSources = new HashMap<>();

        for (Class<? extends ArtifactSource> source : ref.getSubTypesOf(ArtifactSource.class)) {
            try {
                ArtifactSource s = source.newInstance();
                artifactSources.put(s.getId(), s);

                if (logger.isDebugEnabled()) {
                    logger.debug("Instantiated artifact source " + s);
                }

            } catch (InstantiationException | IllegalAccessException e) {
                logger.warn("Cannot instantiate artifact source " + source, e);
            }
        }

        if (artifactSources.isEmpty()) {
            throw new UnsatisfiedDependencyException("Cannot detect any aritfact sources!");
        }

        logger.info("Instantiated {} artifact sources", artifactSources.size());
    }
}
