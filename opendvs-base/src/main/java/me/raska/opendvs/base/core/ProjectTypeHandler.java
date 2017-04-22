package me.raska.opendvs.base.core;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import me.raska.opendvs.base.model.artifact.Artifact;
import me.raska.opendvs.base.model.project.Project;
import me.raska.opendvs.base.model.project.ProjectType;

public interface ProjectTypeHandler {
    /**
     *
     * @return array of supported types to be provided to project as field type
     */
    String[] getSupportedTypes();

    ProjectType getDescriptor();

    void validate(Project p);

    /**
     * Handle upload file. Implementation is responsible for creating and
     * storing Artifact and ProbeAction object as well as passing it into queue
     * 
     * @param file
     * @throws UnsupportedTypeActionException
     *             if functionality is not supported
     * @return generated ProbeAction to be handled by worker
     */
    Artifact handleUpload(Project project, MultipartFile file) throws IOException;

    Artifact triggerScan(Project project, Artifact art);

    Artifact handleWebHook(Project project, HttpServletRequest request, HttpServletResponse response);

    /**
     * Pull-type hooks
     * @param projects
     */
    void setupProjectHooks(List<Project> projects);

}