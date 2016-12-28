package me.raska.opendvs.worker.poller;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class MavenRepositoryPollerTest {
    private static MavenRepositoryPoller poller;

    @BeforeClass
    public static void prepare() throws Exception {
        poller = new MavenRepositoryPoller();
    }

    @Test
    public void testProperId() {
        Assert.assertEquals("maven", poller.getId());
    }
}
