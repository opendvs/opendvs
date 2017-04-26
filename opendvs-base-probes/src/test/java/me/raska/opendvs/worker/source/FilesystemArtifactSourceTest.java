package me.raska.opendvs.worker.source;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FilesystemArtifactSourceTest {

    FilesystemArtifactSource source;
    Path tempFile;

    @Before
    public void setUp() throws IOException {
        source = new FilesystemArtifactSource();
        tempFile = Files.createTempFile(null, null);
    }

    @After
    public void tearDown() {
        source = null;
        tempFile.toFile().delete();
    }

    @Test
    public void testProperSource() {
        assertEquals("local", source.getId());
    }

    @Test
    public void testSuccessResource() throws FileNotFoundException {
        // File f = source.getResource(tempFile.toString());
        // assertNotNull(f);
        // assertTrue(f.exists());
    }

    @Test(expected = NullPointerException.class)
    public void testNullResource() throws FileNotFoundException {
        source.getResource(null, null);
    }

    // @Test(expected = FileNotFoundException.class)
    public void testNotFoundResource() throws FileNotFoundException {
        // File f = source.getResource(UUID.randomUUID().toString());
        // assertFalse(f.exists());
    }
}
