package me.raska.opendvs.core.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import me.raska.opendvs.base.core.ProjectTypeHandler;
import me.raska.opendvs.base.model.artifact.Artifact;
import me.raska.opendvs.base.model.artifact.Artifact.Type;
import me.raska.opendvs.base.model.probe.ProbeAction;
import me.raska.opendvs.base.model.project.Project;
import me.raska.opendvs.base.model.project.ProjectType;
import me.raska.opendvs.base.probe.amqp.ProbeRabbitService;
import me.raska.opendvs.core.dto.ArtifactRepository;
import me.raska.opendvs.core.dto.ProbeActionRepository;
import me.raska.opendvs.core.exception.InvalidRequestException;

@Component
public class GitProjectTypeHandler implements ProjectTypeHandler {

    private ProjectType descriptor;

    @Autowired
    private ArtifactRepository artifactRepository;

    @Autowired
    private ProbeActionRepository probeActionRepository;

    @Autowired
    @Qualifier(ProbeRabbitService.WORKER_QUALIFIER)
    private RabbitTemplate rabbitTemplate;

    @Override
    public String[] getSupportedTypes() {
        return new String[] { "git" };
    }

    @Override
    public Artifact handleUpload(Project project, MultipartFile file) throws IOException {
        throw new InvalidRequestException("Upload is not supported by this type");
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
        List<ProjectType.Property> properties = new ArrayList<>();
        properties.add(ProjectType.Property.builder().key("uri").name("Git URI")
                .description("URI for project, supports both SSH and HTTPS transport").build());
        properties.add(ProjectType.Property.builder().key("private_key").name("SSH Private key")
                .description("Private key in PEM format in case project is not publicly readable").build());

        descriptor = ProjectType.builder().id("git").name("Git project type")
                .description("Remote git projects, check is triggered per specified filter").properties(properties)
                .build();
    }

    @Override
    public void validate(Project p) {
        if (p.getTypeProperties() == null || !p.getTypeProperties().containsKey("uri")
                || p.getTypeProperties().get("uri") == null || p.getTypeProperties().get("uri").isEmpty()) {
            throw new InvalidRequestException("Missing required type property `uri`");
        }

        // TODO: refactor to dynamically check agains descriptor
        if (p.getTypeProperties().size() > 2
                || p.getTypeProperties().size() > 1 && !p.getTypeProperties().containsKey("private_key")) {
            throw new InvalidRequestException("Obtained excessive type properties");
        }

        // TODO: check URI is indeed Git repository 
    }

    @Override
    public Artifact triggerScan(Project project, Artifact art) {
        // TODO: obtain latest ref if identity is branch name
        if (art.getIdentity() == null) {
            throw new InvalidRequestException("Identity ref is required!");
        }

        art.setType(Type.source);
        art.setUri(project.getTypeProperties().get("uri"));
        art.setSourceType(project.getType());
        art.setName(art.getIdentity().substring(0, 7));
        art.setProject(project);
        art.setComponents(null);
        art.setId(null);

        Artifact artifact = artifactRepository.save(art);

        ProbeAction action = new ProbeAction();
        action.setArtifact(artifact);
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
}
