package me.raska.opendvs.worker.source;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.util.FS;
import org.springframework.util.FileSystemUtils;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import me.raska.opendvs.base.artifact.ArtifactSource;
import me.raska.opendvs.base.model.artifact.Artifact;

public class GitProjectSource implements ArtifactSource {

    @Override
    public String getId() {
        return "git";
    }

    private SshSessionFactory generateSshSessionFactory(String privateKey) {
        return new JschConfigSessionFactory() {
            @Override
            protected void configure(Host host, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                JSch jsch = super.createDefaultJSch(fs);
                jsch.addIdentity("key", privateKey.getBytes(StandardCharsets.US_ASCII), null, null);
                return jsch;
            }
        };
    }

    @Override
    public File getResource(Artifact art, Map<String, String> typeProperties) throws Exception {
        Path tmpdir = Files.createTempDirectory(null);

        CloneCommand cloneCommand = Git.cloneRepository().setURI(art.getUri()).setDirectory(tmpdir.toFile());

        String privateKey = typeProperties.get("private_key");
        if (privateKey != null && !privateKey.isEmpty()) {
            final SshSessionFactory sshSessionFactory = generateSshSessionFactory(privateKey);

            cloneCommand.setTransportConfigCallback(transport -> {
                SshTransport sshTransport = (SshTransport) transport;
                sshTransport.setSshSessionFactory(sshSessionFactory);
            });
        }

        Git git = cloneCommand.call();
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
