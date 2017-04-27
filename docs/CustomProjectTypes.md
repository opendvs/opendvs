# Implementing custom Project Types
For implementing additional Project Type (Project Source), implementation must be done on Core and Probe sides.

## Core
On Core side you must implement `me.raska.opendvs.base.core.ProjectTypeHandler` described by following interface:
```
public interface ProjectTypeHandler {
    String[] getSupportedTypes();

    ProjectType getDescriptor();

    void validate(Project p);

    Artifact handleUpload(Project project, MultipartFile file) throws IOException;

    Artifact triggerScan(Project project, Artifact art);

    Artifact handleWebHook(Project project, HttpServletRequest request, HttpServletResponse response);

    void setupProjectHooks(List<Project> projects);
}
```

### getSupportedTypes
This method should return all project identificators this type can handle.

### getDescriptor
This method should return UI descriptor for current type. The descriptor is instance of `ProjectType` class and provides builder for easy creation. In descriptor you specify identificator, name, description and list of properties the type accepts (for example SSH key).

> Supplied property types will be stored into `Project` object.

### handleUploaded(Project, MultipartFile)
This method is called on manual Artifact upload. You should throw `UnsupportedTypeActionException` in case this functionality is unsupported.

> You are responsible for creating Artifact along with ProbeAction, storing those in DB and sending Probing request. See `FilesystemProjectTypeHandler` for example.

### triggerScan(Project, Artifact)
This method is called on manual trigger (`/api/v1/projects/{id}/trigger`). You should throw `UnsupportedTypeActionException` in case this functionality is unsupported.

> You are responsible for creating new Artifact along with ProbeAction, storing those in DB and sending Probing request. See `GitProjectTypeHandler` for example.

### handleWebHook(Project, HttpServletRequest, HttpServletResponse)
This method should be used for integrating custom webhooks into the platform. In future this endpoint is meant to be accessed unsecurely, hence security should be handled with caution.

### setupProjectHooks
This method is called on application startup and project creation with all projects supporting this type for setting up polling-type projects. The functionality is currently unimplemented and will provide scheduling functionality in future (via Quartz).


## Probe
On Probe side you are responsible for fetching required artifact from supported Project Type. You must implement `me.raska.opendvs.base.artifact.ArtifactSource` described by following interface:
```
public interface ArtifactSource {
    String getId();

    File getResource(Artifact art, Map<String, String> typeProperties) throws Exception;

    void cleanupResource(File f) throws Exception;
}
```

> For determining which ArtifactSource should be invoked, `Artifact.sourceType` is matched against `getId` method. SourceType should be usually matching project descriptor ID created in `ProjectTypeHandler.getDescriptor` method.

### getResource(Artifact, Map)
This method should fetch required resources and store them in temporary folders.

### cleanupResource(File)
Thie method is called upon end of probing and should cleanup all resources managed by this Source.
