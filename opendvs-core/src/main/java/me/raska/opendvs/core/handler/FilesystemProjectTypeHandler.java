package me.raska.opendvs.core.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import me.raska.opendvs.base.core.ProjectTypeHandler;
import me.raska.opendvs.base.model.artifact.Artifact;
import me.raska.opendvs.base.model.artifact.Artifact.Type;
import me.raska.opendvs.base.model.probe.ProbeAction;
import me.raska.opendvs.base.model.project.Project;
import me.raska.opendvs.base.model.project.ProjectType;
import me.raska.opendvs.base.probe.amqp.ProbeRabbitService;
import me.raska.opendvs.base.util.Util;
import me.raska.opendvs.core.dto.ArtifactRepository;
import me.raska.opendvs.core.dto.ProbeActionRepository;
import me.raska.opendvs.core.exception.InvalidRequestException;

@Component
public class FilesystemProjectTypeHandler implements ProjectTypeHandler {

    private ProjectType descriptor;

    @Autowired
    private ArtifactRepository artifactRepository;

    @Autowired
    private ProbeActionRepository probeActionRepository;

    @Autowired
    @Qualifier(ProbeRabbitService.WORKER_QUALIFIER)
    private RabbitTemplate rabbitTemplate;

    @Value("${project.handler.filesystem.dir:/tmp}")
    private String artifactDir;

    @Override
    public String[] getSupportedTypes() {
        return new String[] { "local" };
    }

    @Override
    public Artifact handleUpload(Project project, MultipartFile file) throws IOException {
        if (file == null) {
            throw new InvalidRequestException("File cannot be null!");
        }

        File f = Files.createTempFile(Paths.get(artifactDir), null, null).toFile();
        file.transferTo(f);

        Artifact artifact = new Artifact();
        artifact.setType(Type.build);
        artifact.setSourceType(project.getType());
        artifact.setUri(f.getAbsolutePath());
        artifact.setName(file.getOriginalFilename());
        artifact.setIdentity(Util.getFileSha1Checksum(f));
        artifact.setProject(project);
        artifact.setInitiated(new Date());
        artifact.setState(Artifact.State.DETECTING);
        Artifact art = artifactRepository.save(artifact);

        ProbeAction action = new ProbeAction();
        action.setArtifact(art);
        action.setInitiated(new Date());
        action.setState(ProbeAction.State.QUEUED);
        action.setMaxIterations(10);

        ProbeAction act = probeActionRepository.save(action);
        // don't pass the project to the queue
        art.setProject(null);
        art.setProbeAction(act);

        rabbitTemplate.convertAndSend(act);

        return art;
    }

    @Override
    public void setupProjectHooks(List<Project> projects) {
        // TODO Auto-generated method stub

    }

    @Override
    public ProjectType getDescriptor() {
        return descriptor;
    }

    @PostConstruct
    private void init() {
        descriptor = ProjectType.builder().id("local").name("Local project type")
                .description(
                        "For projects that don't want to expose the artifacts, user has to upload artifact directly via UI / API")
                .properties(Arrays.asList()).build();
    }

    @Override
    public void validate(Project p) {
        if (p.getTypeProperties() != null && p.getTypeProperties().size() > 0) {
            throw new InvalidRequestException("Project properties are not applicable for this type");
        }
    }

    @Override
    public Artifact triggerScan(Project project, Artifact art) {
        throw new InvalidRequestException("Triggering isn't supported for this type. Upload the artifact instead!");
    }
}
