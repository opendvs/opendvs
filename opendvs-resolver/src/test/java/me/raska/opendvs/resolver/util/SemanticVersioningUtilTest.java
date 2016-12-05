package me.raska.opendvs.resolver.util;

import java.util.Date;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import me.raska.opendvs.base.model.Component;
import me.raska.opendvs.base.model.ComponentVersion;
import me.raska.opendvs.base.model.artifact.ArtifactComponent.State;
import me.raska.opendvs.resolver.model.SemverEntry;

public class SemanticVersioningUtilTest {

    private static Component c1;
    private static Component c2;

    @BeforeClass
    public static void prepareData() {
        String[] versions = { "1.2.3", "1.2.2", "1.2.1", "1.1.2", "1.1.1", "1.0.1", "1.0.0", "0.9.9", "0.9.8" };
        c1 = new Component();
        c1.setLatestVersion("1.2.3");
        c1.setVersions(new HashSet<>());

        for (int i = 0; i < versions.length; i++) {
            ComponentVersion cv = new ComponentVersion();
            cv.setVersion(versions[i]);
            cv.setPublished(new Date(System.currentTimeMillis() - SemanticVersioningUtil.DAY_VAL * 20 - i * 1000));
            c1.getVersions().add(cv);
        }

        String[] versions2 = { "1.2.3.RELEASE", "1.2.2.RELEASE" };
        c2 = new Component();
        c2.setLatestVersion("1.2.3.RELEASE");
        c2.setVersions(new HashSet<>());

        for (int i = 0; i < versions2.length; i++) {
            ComponentVersion cv = new ComponentVersion();
            cv.setVersion(versions2[i]);
            c2.getVersions().add(cv);
        }

    }

    @Test
    public void testEntryPrefixGeneration() {
        Assert.assertEquals(SemanticVersioningUtil.buildSemverPrefix(new SemverEntry(1, 2, 3)), "1.");
        Assert.assertEquals(SemanticVersioningUtil.buildSemverPrefix(new SemverEntry(1, 2, 3, '~')), "1.2.");
        Assert.assertEquals(SemanticVersioningUtil.buildSemverPrefix(new SemverEntry(1, 2, 3, '^')), "1.");
    }

    @Test
    public void testProperSemverParsing() {
        Assert.assertNull(SemanticVersioningUtil.parseSemver(null));
        Assert.assertNull(SemanticVersioningUtil.parseSemverRegex(null));
        Assert.assertEquals(SemanticVersioningUtil.parseSemverRegex("1.2.3"), new SemverEntry(1, 2, 3));
        Assert.assertEquals(SemanticVersioningUtil.parseSemverRegex("1.s.3"), null);
        Assert.assertEquals(SemanticVersioningUtil.parseSemverRegex("0.0.0"), new SemverEntry(0, 0, 0));
        Assert.assertEquals(SemanticVersioningUtil.parseSemverRegex("^1.2.3"), null);
        Assert.assertEquals(SemanticVersioningUtil.parseSemverRegex("~1.2.3"), null);
        Assert.assertEquals(SemanticVersioningUtil.parseSemverRegex("*"), null);
        Assert.assertEquals(SemanticVersioningUtil.parseSemver("1.2.3"), new SemverEntry(1, 2, 3));
        Assert.assertEquals(SemanticVersioningUtil.parseSemver("^1.2.3"), new SemverEntry(1, 2, 3, '^'));
        Assert.assertEquals(SemanticVersioningUtil.parseSemver("~1.2.3"), new SemverEntry(1, 2, 3, '~'));
        Assert.assertEquals(SemanticVersioningUtil.parseSemver("#1.2.3"), null);
        Assert.assertEquals(SemanticVersioningUtil.parseSemver("*"), new SemverEntry(0, 0, 0, '*'));
    }

    @Test
    public void testSemanticVersionHandling() {
        Assert.assertEquals(SemanticVersioningUtil.checkVersion("1.2.3", c1, 0), State.UP_TO_DATE);
        Assert.assertEquals(SemanticVersioningUtil.checkVersion("1.2.2", c1, 0), State.OUTDATED);
        Assert.assertEquals(SemanticVersioningUtil.checkVersion("^1.2.2", c1, 0), State.UP_TO_DATE);
        Assert.assertEquals(SemanticVersioningUtil.checkVersion("^1.1.2", c1, 0), State.UP_TO_DATE);
        Assert.assertEquals(SemanticVersioningUtil.checkVersion("^0.9.9", c1, 0), State.OUTDATED);
        Assert.assertEquals(SemanticVersioningUtil.checkVersion("1.2.0", c1, 0), State.UNKNOWN);
        Assert.assertEquals(SemanticVersioningUtil.checkVersion("^1.2.0", c1, 0), State.UP_TO_DATE);
        Assert.assertEquals(SemanticVersioningUtil.checkVersion("1.2.1", c1, 30), State.OUTDATED);
        Assert.assertEquals(SemanticVersioningUtil.checkVersion("1.1.1", c1, 30), State.OUTDATED);
        Assert.assertEquals(SemanticVersioningUtil.checkVersion("1.1.2", c1, 30), State.OUTDATED);
        Assert.assertEquals(SemanticVersioningUtil.checkVersion("1.1.2", c1, 10), State.OUTDATED);
        Assert.assertEquals(SemanticVersioningUtil.checkVersion("^1.1.1", c1, 30), State.UP_TO_DATE);
        Assert.assertEquals(SemanticVersioningUtil.checkVersion("^0.9.8", c1, 30), State.UP_TO_DATE);
        Assert.assertEquals(SemanticVersioningUtil.checkVersion("^0.9.9", c1, 30), State.UP_TO_DATE);
        Assert.assertEquals(SemanticVersioningUtil.checkVersion("~1.1.2", c1, 30), State.UP_TO_DATE);
        Assert.assertEquals(SemanticVersioningUtil.checkVersion("~1.0.1", c1, 30), State.UP_TO_DATE);
        Assert.assertEquals(SemanticVersioningUtil.checkVersion("~1.0.0", c1, 10), State.OUTDATED);
        Assert.assertEquals(SemanticVersioningUtil.checkVersion("~1.0.5", c1, 10), State.UNKNOWN);
        Assert.assertEquals(SemanticVersioningUtil.checkVersion("*", c1, 30), State.UP_TO_DATE);
        Assert.assertEquals(SemanticVersioningUtil.checkVersion("*", c1, 0), State.UP_TO_DATE);
        Assert.assertEquals(SemanticVersioningUtil.checkVersion("*", null, 0), State.UNKNOWN);
    }

    @Test
    public void testNonSemanticVersionHandling() {
        Assert.assertEquals(SemanticVersioningUtil.checkVersion("1.2.3.RELEASE", c2, 0), State.UP_TO_DATE);
        Assert.assertEquals(SemanticVersioningUtil.checkVersion("1.2.2.RELEASE", c2, 0), State.OUTDATED);
        Assert.assertEquals(SemanticVersioningUtil.checkVersion("1.2.1.RELEASE", c2, 0), State.UNKNOWN);
        Assert.assertEquals(SemanticVersioningUtil.checkVersion("1.2.1.RELEASE", null, 0), State.UNKNOWN);
        Assert.assertEquals(SemanticVersioningUtil.checkVersion("", null, 0), State.UNKNOWN);
        Assert.assertEquals(SemanticVersioningUtil.checkVersion(null, null, 0), State.UNKNOWN);

    }
}
