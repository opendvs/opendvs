package me.raska.opendvs.base.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import lombok.extern.slf4j.Slf4j;
import me.raska.opendvs.base.model.probe.ProbeActionStep;
import me.raska.opendvs.base.model.probe.ProbeActionStep.State;

@Slf4j
public class Util {
    public static void unzip(File input, File output) throws IOException {
        if (output.exists() && !output.isDirectory()) {
            throw new IllegalArgumentException("File exists and is not directory");
        }
        // try to create it
        if (!output.exists()) {
            output.mkdir();
        }

        byte[] buffer = new byte[1024];

        // autoclose
        try (ZipInputStream is = new ZipInputStream(new FileInputStream(input))) {
            ZipEntry ze = is.getNextEntry();
            Path basePath = output.toPath();

            while (ze != null) {
                if (!ze.isDirectory()) {
                    File out = basePath.resolve(ze.getName().replace("\\.\\.", "")).toFile();
                    Files.createDirectories(out.getParentFile().toPath());

                    // autoclose
                    try (FileOutputStream os = new FileOutputStream(out)) {
                        int len;
                        while ((len = is.read(buffer)) > 0) {
                            os.write(buffer, 0, len);
                        }
                    }
                }
                ze = is.getNextEntry();
            }
        }
    }

    public static String getFileSha1Checksum(File file) {
        try {
            // TODO: reuse MessageDigest
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] data = new byte[1024];
                int count = 0;

                while ((count = fis.read(data)) != -1) {
                    digest.update(data, 0, count);
                }
            }
            byte[] bytes = digest.digest();

            // convert to hexa, StringBuilder is not synchronized, so
            // faster than StringBuffer in this case
            StringBuilder sb = new StringBuilder();
            sb.append("sha1:");
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch (Exception e) {
            log.error("Cannot convert file " + file + " to sha-1 checksum", e);
            return null;
        }
    }

    public static ProbeActionStep generateErrorStep(String msg) {
        ProbeActionStep step = new ProbeActionStep();
        step.setState(State.FAILURE);
        step.setOutput(msg);
        return step;
    }
}
