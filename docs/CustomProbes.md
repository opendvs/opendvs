# Implementing custom Probes

To implement custom Probes (e.g. requirements.txt detection), you need to create implementation of `me.raska.opendvs.base.probe.NativeProbe` interface, which is specified by following definition:
```
public interface NativeProbe {

    /**
     * Detects resources that should be extracted and does so.
     *
     * @param artifact
     * @param extractedComponents
     * @param context
     * @return list of all executed steps
     */
    List<ProbeActionStep> extract(Artifact artifact, List<ArtifactComponent> extractedComponents,
            ProbingContext context);

    /**
     * Detects components in artifact. Is called before extract method.
     * Extracted artifacts will get passed in next iteration
     * 
     * @param artifact
     * @param extractedComponents
     * @param context
     * @return
     */
    List<ProbeActionStep> detectComponents(Artifact artifact, List<ArtifactComponent> extractedComponents,
            ProbingContext context);
}
```

> ProbingContext specifies helper methods to obtain directory of extracted components. For logging component paths, use `context.getStrippedFilePath` to ensure no server-related data are leaked.

## detectComponents(Artifact, List<ArtifactComponent>, ProbingContext)
This method is called prior to extract method and is responsible for sole detection of required resources inside `extractedComponents` directories. You should return all ProbeActionSteps (e.g. one for each detected resource) where detected components should be described. 

> If `extractedComponents` list is empty, assume this is the first run and no components were extracted yet. You can then use `context.getArtifactFile()` as a base (e.g. cloned Git repo).

For each detected component you are responsible for filling following fields:
+ *group* - component group, e.g. maven
+ *name* - descriptive name of the component
+ *version* - if available
+ *hash* - if available
+ *uid* - reconstructable identifier of the component, e.g. `maven:<groupId>:<artifactId>:<version>`, **should include version for correct graph reconstruction**
+ *scope* - application scope of the component (runtime, test, etc.), use compile as default
+ *parentUid* - UID of parent component, empty specified top-level component

## extract(Artifact, List<ArtifactComponent>, ProbingContext)
This method is called after detectComponent method and is responsible for extracting any resources of known type (e.g. ZipExtractProbe extracts all ZIP files).
> Don't throw unimplemented exceptions, return empty List instead

You should use `context.getComponentDirectoryPath` to obtain pseudorandom component directory where the extraction should occur. Don't use system-specific folders or different folder creation methods as extracted folders are automatically removed after probing and different usage could cause unwanted behaviour, server storage clogging and data leakage.

You are also responsible of providing detected components with required fields as in `detectComponents` method. Those will be processed and forwarded to `detectComponents` after each iteration.
> For calculating resource hash, you can use `me.raska.opendvs.base.util.Util.getFileSha1Checksum` method.
