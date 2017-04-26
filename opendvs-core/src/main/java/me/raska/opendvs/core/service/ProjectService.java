package me.raska.opendvs.core.service;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import me.raska.opendvs.base.core.ProjectTypeHandler;
import me.raska.opendvs.base.core.amqp.CoreRabbitService;
import me.raska.opendvs.base.core.event.ArtifactUpdateEvent;
import me.raska.opendvs.base.model.artifact.Artifact;
import me.raska.opendvs.base.model.project.Project;
import me.raska.opendvs.base.model.project.ProjectType;
import me.raska.opendvs.core.dto.ArtifactRepository;
import me.raska.opendvs.core.dto.PollerActionRepository;
import me.raska.opendvs.core.dto.ProjectRepository;
import me.raska.opendvs.core.exception.InvalidRequestException;
import me.raska.opendvs.core.exception.NotFoundException;
import me.raska.opendvs.core.rest.filtering.Filterable;
import me.raska.opendvs.core.rest.filtering.FilterableSpecification;

@Slf4j
@Service
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ArtifactRepository artifactRepository;

    @Autowired
    private PollerActionRepository pollerActionRepository;

    @Autowired
    private UserSession userSession;

    @Autowired
    private UserSecurityService userSecurityService;

    @Autowired
    private FilterableSpecification filterableSpec;

    @Autowired
    @Qualifier(CoreRabbitService.FANOUT_QUALIFIER)
    private RabbitTemplate fanoutTemplate;

    private Map<String, ProjectTypeHandler> projectHandlers;

    public List<ProjectType> getAvailableHandlers() {
        return projectHandlers.values().stream().map(ProjectTypeHandler::getDescriptor).collect(Collectors.toList());
    }

    public Page<Project> getAvailableProjects(Pageable p, Filterable filter) {
        if (userSession.isAdmin()) {
            return projectRepository.findAll(filterableSpec.handleEntityFiltering(Project.class, filter), p);
        }

        final Map<String, Set<String>> inMap = new HashMap<>();
        inMap.put("id", userSession.getUser().getRoles());
        return projectRepository.findAll(filterableSpec.handleEntityFiltering(Project.class, filter, null, inMap), p);
    }

    @PreAuthorize("hasAuthority(#id) or hasAuthority('ADMIN')")
    public Project getProject(String id) {
        Project p = projectRepository.findOne(id);
        if (p == null) {
            throw new NotFoundException("Project doesn't exist");
        }
        return p;
    }

    @PreAuthorize("hasAuthority(#id) or hasAuthority('ADMIN')")
    public Page<Artifact> getProjectArtifacts(String id, Pageable page, Filterable filter) {
        Project project = getProject(id);

        final Map<String, Object> whereMap = new HashMap<>();
        whereMap.put("project", project);

        Pageable pageable = page;
        if (page.getSort() == null) {
            pageable = new PageRequest(page.getPageNumber(), page.getPageSize(), new Sort(Direction.DESC, "initiated"));
        }

        Page<Artifact> p = artifactRepository
                .findAll(filterableSpec.handleEntityFiltering(Artifact.class, filter, whereMap), pageable);

        // cleanup
        p.getContent().forEach(a -> {
            a.setComponents(null);
            a.setProject(null);
            if (a.getProbeAction() != null) {
                a.getProbeAction().setSteps(null);
            }
        });
        return p;
    }

    @PreAuthorize("hasAuthority(#project) or hasAuthority('ADMIN')")
    public Artifact getProjectArtifact(String project, String artifact) {
        Artifact art = artifactRepository.findOne(artifact);

        if (art == null || art.getProject() == null || !project.equals(art.getProject().getId())) {
            throw new NotFoundException("Artifact doesn't exist or doesn't belong to this project");
        }

        return art;
    }

    @Transactional
    public Project createProject(Project p) {
        p.setId(null);
        p.setArtifacts(null);
        if (!projectHandlers.containsKey(p.getType())) {
            throw new InvalidRequestException("Provided type is not supported");
        }
        projectHandlers.get(p.getType()).validate(p);

        Project project = projectRepository.save(p);

        userSecurityService.updateUserAuthorities(project.getId());
        return project;
    }

    @PreAuthorize("hasAuthority(#projectId) or hasAuthority('ADMIN')")
    public Artifact triggerScan(String projectId, Artifact artifact) {
        Project p = projectRepository.findOne(projectId);
        if (p == null) {
            throw new NotFoundException("Project doesn't exist");
        }

        if (!projectHandlers.containsKey(p.getType())) {
            throw new InvalidRequestException("Project type is not supported");
        }

        artifact.setInitiated(new Date());
        return projectHandlers.get(p.getType()).triggerScan(p, artifact);
    }

    public Artifact handleWebHook(String projectId, HttpServletRequest request, HttpServletResponse response) {
        Project p = projectRepository.findOne(projectId);
        if (p == null) {
            throw new NotFoundException("Project doesn't exist");
        }

        if (!projectHandlers.containsKey(p.getType())) {
            throw new InvalidRequestException("Project type is not supported");
        }

        return projectHandlers.get(p.getType()).handleWebHook(p, request, response);
    }

    @PreAuthorize("hasAuthority(#projectId) or hasAuthority('ADMIN')")
    public Artifact uploadArtifact(String projectId, MultipartFile file) {
        Project p = projectRepository.findOne(projectId);
        if (p == null) {
            throw new NotFoundException("Project doesn't exist");
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

    @Transactional
    public void resolveArtifactsState() {
        for (Artifact artifact : artifactRepository.findByState(Artifact.State.RESOLVING)) {
            if (pollerActionRepository.getRunningActions(artifact.getId()) == 0) {
                artifact.setState(Artifact.State.FINISHED);
                Artifact savedArtifact = artifactRepository.save(artifact);

                // create shallow copy to avoid sending unnecessary data
                Artifact art = savedArtifact.clone();
                Project prj = savedArtifact.getProject().clone();

                fanoutTemplate.convertAndSend(new ArtifactUpdateEvent(art, prj));

                if (log.isDebugEnabled()) {
                    log.debug(
                            "Resolved artifact " + art.getId() + " state to FINISHED as there are no running actions");
                }
            }
        }
    }
}
