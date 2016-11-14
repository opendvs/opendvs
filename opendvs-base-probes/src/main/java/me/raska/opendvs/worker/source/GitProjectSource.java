package me.raska.opendvs.worker.source;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jgit.api.Git;
import org.springframework.util.FileSystemUtils;

import me.raska.opendvs.base.artifact.ArtifactSource;
import me.raska.opendvs.base.model.artifact.Artifact;

public class GitProjectSource implements ArtifactSource {

    @Override
    public String getId() {
        return "git";
    }

    @Override
    public File getResource(Artifact art) throws Exception {
        Path tmpdir = Files.createTempDirectory(null);
        Git git = Git.cloneRepository().setURI(art.getUri()).setDirectory(tmpdir.toFile()).call();
        git.checkout().setName(art.getIdentity()).call();

        return tmpdir.toFile();
    }

    @Override
    public void cleanupResource(File f) throws Exception {
        // delete the file
        if (!FileSystemUtils.deleteRecursively(f)) {
            throw new IOException("Couldn't recursively delete " + f);
        }

    }

}
