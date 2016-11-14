package me.raska.opendvs.base.probe;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.util.FileSystemUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import me.raska.opendvs.base.model.artifact.ArtifactComponent;

@Data
public class ProbingContext {
    private final Integer lock = 0;

    private File artifactFile;
    private boolean debug;

    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    private Path componentRoot;

    public void setArtifactFile(File file) throws IOException {
        this.artifactFile = file;

        // create Component root path
        getComponentRootPath().toFile().mkdir();
    }

    public Path getComponentDirectoryPath(ArtifactComponent component) throws IOException {
        return getComponentRootPath().resolve(component.getUid());
    }

    public void cleanup() throws IOException {
        if (componentRoot != null) {
            if (!FileSystemUtils.deleteRecursively(componentRoot.toFile())) {
                throw new IOException("Cannot delete all entries in " + componentRoot);
            }
        }
    }

    private Path getComponentRootPath() throws IOException {
        // this synchronization should be enough if paralellism is used for some
        // reason
        synchronized (lock) {
            if (componentRoot == null) {
                componentRoot = Files.createTempDirectory(null);
            }
        }

        return componentRoot;
    }

    public String getStrippedFilePath(File f) {
        return f.getAbsolutePath().replace(artifactFile.getAbsolutePath(), "").replace(componentRoot.toString(), "");
    }
}
