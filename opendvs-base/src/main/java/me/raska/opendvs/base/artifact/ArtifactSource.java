package me.raska.opendvs.base.artifact;

import java.io.File;

import me.raska.opendvs.base.model.artifact.Artifact;

public interface ArtifactSource {
    String getId();

    File getResource(Artifact art) throws Exception;

    void cleanupResource(File f) throws Exception;
}
