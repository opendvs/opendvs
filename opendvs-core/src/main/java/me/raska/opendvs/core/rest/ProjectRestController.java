package me.raska.opendvs.core.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import me.raska.opendvs.base.model.artifact.Artifact;
import me.raska.opendvs.base.model.project.Project;
import me.raska.opendvs.base.model.project.ProjectType;
import me.raska.opendvs.core.service.ProjectService;

@CrossOrigin
@RestController
@RequestMapping("/api/v1")
public class ProjectRestController {
    @Autowired
    private ProjectService projectService;

    @RequestMapping(value = "/project/types", method = RequestMethod.GET)
    public List<ProjectType> getTypes() {
        return projectService.getAvailableHandlers();
    }

    @RequestMapping(value = "/projects", method = RequestMethod.GET)
    public Page<Project> getAvailableProjects(Pageable p) {
        return projectService.getAvailableProjects(p);
    }

    @RequestMapping(value = "/project/{id}", method = RequestMethod.GET)
    public Project getProject(@PathVariable("id") String projectId) {
        return projectService.getProject(projectId);
    }

    @RequestMapping(value = "/project/{id}/artifacts", method = RequestMethod.GET)
    public Page<Artifact> getProjectArtifacts(@PathVariable("id") String projectId, Pageable p) {
        Page<Artifact> page = projectService.getProjectArtifacts(projectId, p);

        page.getContent().forEach(a -> {
            a.setComponents(null);
            a.setProject(null);
            if (a.getProbeAction() != null) {
                a.getProbeAction().setSteps(null);
            }
        });
        return page;
    }

    @RequestMapping(value = "/project/{id}/artifact/{artifactId}", method = RequestMethod.GET)
    public Artifact getProjectArtifact(@PathVariable("id") String projectId,
            @PathVariable("artifactId") String artifactId) {
        return projectService.getProjectArtifact(projectId, artifactId);
    }

    @RequestMapping(value = "/projects", method = RequestMethod.POST)
    public Project createProject(@RequestBody Project p) {
        return projectService.createProject(p);
    }

    @RequestMapping(value = "/project/{id}/upload", method = RequestMethod.POST)
    public Artifact uploadArtifact(@PathVariable("id") String projectId,
            @RequestParam(name = "artifact") MultipartFile file) {
        return projectService.uploadArtifact(projectId, file);
    }

    @RequestMapping(value = "/project/{id}/trigger", method = RequestMethod.POST)
    public Artifact triggetScan(@PathVariable("id") String projectId, @RequestBody Artifact artifact) {
        return projectService.triggerScan(projectId, artifact);
    }
}
