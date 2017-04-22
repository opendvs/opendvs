package me.raska.opendvs.worker.probe;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.invoker.PrintStreamHandler;
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
    private static final String GROUP_NAME = "maven";
    private static final String DEFAULT_SCOPE = "runtime";
    private static final String SEPARATOR = ":";

    private static final Logger logger = LoggerFactory.getLogger(MavenProbe.class);

    // TODO: load from context
    private String mavenHome = "/usr/share/maven";

    @Override
    public List<ProbeActionStep> extract(Artifact artifact, List<ArtifactComponent> extractedComponents,
            ProbingContext context) {
        return Arrays.asList();
    }

    private boolean pomMatches(File f) {
        return "pom.xml".equals(f.getName());
    }

    private void executeMavenCommand(File pom, List<String> goals) throws MavenInvocationException, IOException {

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(pom);
        request.setGoals(goals);

        Invoker inv = new DefaultInvoker();
        inv.setMavenHome(new File(mavenHome));
        inv.setOutputHandler(null);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        inv.setErrorHandler(new PrintStreamHandler(ps, true));

        InvocationResult res = inv.execute(request);
        if (res.getExitCode() != 0) {
            throw new MavenInvocationException("Cannot run '" + goals + "', execution returned exit code "
                    + res.getExitCode() + "\n" + new String(os.toByteArray(), StandardCharsets.UTF_8));
        }
    }

    private Model getPomModel(File pom) throws IOException, XmlPullParserException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        return reader.read(new FileReader(pom));
    }

    private File generateEffectivePom(File pom) throws IOException, MavenInvocationException {
        File tempfile = Files.createTempFile(null, ".pom").toFile();
        tempfile.deleteOnExit();

        try {
            executeMavenCommand(pom, Arrays.asList("help:effective-pom", "-Doutput=" + tempfile.getAbsolutePath()));
        } catch (Exception e) {
            if (!tempfile.delete()) {
                logger.warn("Cannot delete tempfile " + tempfile.getAbsolutePath());
            }
            throw e;
        }

        return tempfile;
    }

    private File generateDependencyTree(File pom) throws IOException, MavenInvocationException {
        File tempfile = Files.createTempFile(null, ".pom").toFile();
        tempfile.deleteOnExit();

        try {
            executeMavenCommand(pom,
                    Arrays.asList("dependency:tree", "-DoutputType=tgf", "-DoutputFile=" + tempfile.getAbsolutePath()));
        } catch (Exception e) {
            if (!tempfile.delete()) {
                logger.warn("Cannot delete tempfile " + tempfile.getAbsolutePath());
            }
            throw e;
        }

        return tempfile;
    }

    private List<ArtifactComponent> handlePomDependencies(File pom, ArtifactComponent parentComponent)
            throws IOException, XmlPullParserException {
        final List<ArtifactComponent> components = new ArrayList<>();
        final Model model = getPomModel(pom);

        final ArtifactComponent parentc = new ArtifactComponent();
        parentc.setGroup(GROUP_NAME);
        parentc.setVersion(model.getVersion());
        parentc.setName(model.getGroupId() + SEPARATOR + model.getArtifactId());
        parentc.setUid(GROUP_NAME + SEPARATOR + parentc.getName() + SEPARATOR + parentc.getVersion());
        parentc.setParentUid((parentComponent != null) ? parentComponent.getUid() : null);
        parentc.setScope(DEFAULT_SCOPE);
        components.add(parentc);

        Map<String, String> properties = mapMavenProperties(model);
        for (Dependency dep : model.getDependencies()) {
            final ArtifactComponent c = new ArtifactComponent();
            c.setScope((dep.getScope() == null) ? DEFAULT_SCOPE : dep.getScope());
            c.setGroup(GROUP_NAME);

            String version = dep.getVersion();
            if (version != null) {
                // replace all unresolved properties with this nasty hack
                for (Entry<String, String> entry : properties.entrySet()) {
                    version = version.replace(entry.getKey(), entry.getValue());
                }
                c.setVersion(version);
            }

            c.setName(dep.getGroupId() + SEPARATOR + dep.getArtifactId());
            c.setUid(GROUP_NAME + SEPARATOR + c.getName() + SEPARATOR + c.getVersion());
            c.setParentUid(parentc.getUid());
            components.add(c);
        }

        return components;
    }

    /**
     * Handle TGF format vertex as defined by 'id<spacing>name'.
     * 
     * @param line
     *            line with vertex
     * @param componentsMap
     *            Map to be mutated with id => name mapping
     */
    private void handleTGFVertex(String line, Map<String, ArtifactComponent> componentsMap, ArtifactComponent parentComponent) {
        final String[] parsed = line.split(" ", 2);
        if (parsed.length != 2) {
            logger.warn("Cannot properly parse TGF vertex line '{}'", line);
            return;
        }

        final String[] componentData = parsed[1].split(SEPARATOR);
        if (componentData.length < 4) {
            logger.warn("Cannot parse Maven artifact name from TGF vertex line '{}'", line);
            return;
        }

        final ArtifactComponent component = new ArtifactComponent();
        component.setGroup(GROUP_NAME);
        component.setName(componentData[0] + SEPARATOR + componentData[1]);
        component.setVersion(componentData[3]);
        // runtime for parent-level
        component.setScope((componentData.length > 4) ? componentData[4] : DEFAULT_SCOPE);
        component.setUid(GROUP_NAME + SEPARATOR + component.getName() + SEPARATOR + component.getVersion());
        component.setParentUid((parentComponent != null) ? parentComponent.getUid() : null);

        componentsMap.put(parsed[0], component);
    }

    private void handleTGFEdge(String line, Map<String, ArtifactComponent> componentsMap) {
        final String[] parsed = line.split(" ");
        if (parsed.length < 2) {
            logger.warn("Cannot properly parse TGF edge line '{}'", line);
            return;
        }

        final ArtifactComponent source = componentsMap.get(parsed[0]);
        final ArtifactComponent target = componentsMap.get(parsed[1]);

        if (source == null || target == null) {
            // TODO: also log artifact details
            logger.warn("Cannot link TGF edge with source '{}' and parent '{}'", parsed[0], parsed[1]);
            return;
        }

        target.setParentUid(source.getUid());
    }

    private Collection<ArtifactComponent> handleDependencyTree(File dependencyFile, ArtifactComponent parentComponent)
            throws IOException {
        final Map<String, ArtifactComponent> componentsMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(dependencyFile))) {
            boolean edges = false;
            String line = null;
            while ((line = reader.readLine()) != null) {
                // if separator is encountered, switch states
                if (!edges && "#".equals(line)) {
                    edges = true;
                    continue;
                }

                if (edges) {
                    handleTGFEdge(line, componentsMap);
                } else {
                    handleTGFVertex(line, componentsMap, parentComponent);
                }
            }
        }
        return componentsMap.values();
    }

    private List<ArtifactComponent> getMavenComponents(File pom, ArtifactComponent parentComponent)
            throws IOException, XmlPullParserException {
        final List<ArtifactComponent> components = new ArrayList<>();

        File fallbackPom = pom;
        File dependencyFile = null;
        try {
            dependencyFile = generateDependencyTree(pom);
        } catch (IOException | MavenInvocationException e) {
            logger.info("Cannot build dependency tree for " + pom.getAbsolutePath() + ", trying effective pom", e);
            try {
                fallbackPom = generateEffectivePom(pom);
            } catch (IOException | MavenInvocationException e2) {
                logger.warn("Cannot build dependency tree nor effective pom for " + pom.getAbsolutePath()
                        + ", falling back", e2);
                // fallback to regular pom
            }
        }

        if (dependencyFile == null) {
            components.addAll(handlePomDependencies(fallbackPom, parentComponent));
        } else {
            components.addAll(handleDependencyTree(dependencyFile, parentComponent));
            if (!dependencyFile.delete()) {
                logger.warn("Cannot delete dependencyFile {}", dependencyFile.getAbsolutePath());
            }
        }

        return components;
    }

    private Map<String, String> mapMavenProperties(Model model) {
        // try the best to replace properties
        Map<String, String> map = new HashMap<>();
        model.getProperties().forEach((k, v) -> map.put(String.format("${%s}", k), v.toString()));
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
