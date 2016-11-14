package me.raska.opendvs.worker.source;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import me.raska.opendvs.base.artifact.ArtifactSource;
import me.raska.opendvs.base.model.artifact.Artifact;

public class FilesystemArtifactSource implements ArtifactSource {

    @Override
    public String getId() {
        return "local";
    }

    @Override
    public File getResource(Artifact artifact) throws FileNotFoundException {
        File f = new File(artifact.getUri());
        if (!f.exists()) {
            throw new FileNotFoundException("Resource " + artifact.getUri() + " doesn't exist");
        }

        return f;
    }

    @Override
    public void cleanupResource(File f) throws Exception {
        // cleanup published file
        if (!f.delete()) {
            throw new IOException("Couldn't delete " + f);
        }
    }

}
