package me.raska.opendvs.core.service;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import me.raska.opendvs.base.core.ProjectTypeHandler;
import me.raska.opendvs.base.model.artifact.Artifact;
import me.raska.opendvs.base.model.project.Project;
import me.raska.opendvs.base.model.project.ProjectType;
import me.raska.opendvs.core.dto.ArtifactRepository;
import me.raska.opendvs.core.dto.ProjectRepository;
import me.raska.opendvs.core.exception.InvalidRequestException;

@Slf4j
@Service
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ArtifactRepository artifactRepository;

    private Map<String, ProjectTypeHandler> projectHandlers;

    public List<ProjectType> getAvailableHandlers() {
        return projectHandlers.values().stream().map(p -> p.getDescriptor()).collect(Collectors.toList());
    }

    public Page<Project> getAvailableProjects(Pageable p) {
        return projectRepository.findAll(p);
    }

    public Project getProject(String id) {
        Project p = projectRepository.findOne(id);
        if (p == null) {
            throw new InvalidRequestException("Project doesn't exist");
        }
        return p;
    }

    public Page<Artifact> getProjectArtifacts(String id, Pageable page) {
        Project p = projectRepository.findOne(id);
        if (p == null) {
            throw new InvalidRequestException("Project doesn't exist");
        }

        return artifactRepository.findByProjectOrderByInitiatedDesc(p, page);
    }

    public Artifact getProjectArtifact(String project, String artifact) {
        Artifact art = artifactRepository.findOne(artifact);

        if (art == null || art.getProject() == null || !project.equals(art.getProject().getId())) {
            throw new InvalidRequestException("Artifact doesn't exist or doesn't belong to this project");
        }

        return art;
    }

    public Project createProject(Project p) {
        p.setId(null);
        p.setArtifacts(null);
        if (!projectHandlers.containsKey(p.getType())) {
            throw new InvalidRequestException("Provided type is not supported");
        }
        projectHandlers.get(p.getType()).validate(p);

        return projectRepository.save(p);
    }

    public Artifact triggerScan(String projectId, Artifact artifact) {
        Project p = projectRepository.findOne(projectId);
        if (p == null) {
            throw new InvalidRequestException("Project doesn't exist");
        }

        if (!projectHandlers.containsKey(p.getType())) {
            throw new InvalidRequestException("Project type is not supported");
        }

        artifact.setInitiated(new Date());
        return projectHandlers.get(p.getType()).triggerScan(p, artifact);
    }

    public Artifact uploadArtifact(String projectId, MultipartFile file) {
        Project p = projectRepository.findOne(projectId);
        if (p == null) {
            throw new InvalidRequestException("Project doesn't exist");
        }

        if (!projectHandlers.containsKey(p.getType())) {
            throw new InvalidRequestException("Project type is not supported");
        }

        try {
            return projectHandlers.get(p.getType()).handleUpload(p, file);
        } catch (IOException e) {
            log.error("IOException while uploading artifact for " + projectId, e);
            throw new InvalidRequestException("File canot be uploaded");
        } catch (DataIntegrityViolationException dive) {
            throw new InvalidRequestException("This artifact has already been processed");
        }
    }

    @Autowired
    public void setupHandlers(List<ProjectTypeHandler> handlers) {
        projectHandlers = new HashMap<>();
        handlers.stream().forEach(p -> {
            String[] types = p.getSupportedTypes();
            assert types != null : "Supported types cannot be null for " + p;

            for (int i = 0; i < types.length; i++) {
                projectHandlers.put(types[i], p);
            }
        });
    }
}
