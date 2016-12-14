package me.raska.opendvs.resolver.util;

import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import me.raska.opendvs.base.model.Component;
import me.raska.opendvs.base.model.ComponentVersion;
import me.raska.opendvs.base.model.artifact.ArtifactComponent.State;
import me.raska.opendvs.resolver.model.SemverEntry;

public class SemanticVersioningUtil {
    public static final int DAY_VAL = 1000 * 60 * 24;
    public static final Pattern SEMVER_PATTERN = Pattern.compile("^([\\d]+)\\.([\\d]+)\\.([\\d]+)$");

    public static ComponentVersion getLatestSemanticVersion(Component c, String prefix) {
        if (prefix == null) {
            return null;
        }

        return c.getVersions().stream().filter(v -> v.getVersion().startsWith(prefix))
                .filter(v -> v.getPublished() != null && SEMVER_PATTERN.matcher(v.getVersion()).matches())
                .sorted((v1, v2) -> v2.getPublished().compareTo(v1.getPublished())).findFirst().orElse(null);
    }

    public static State checkVersion(String version, Component c, int majorOffset) {
        if (c == null || version == null) {
            return State.UNKNOWN;
        }

        SemverEntry entry = parseSemver(version);
        Set<String> compVersions = c.getVersions().stream().map(cv -> cv.getVersion()).collect(Collectors.toSet());

        if (entry == null) { // not semantically versioned
            if (version.equals(c.getLatestVersion())) {
                return State.UP_TO_DATE;
            } else if (compVersions.contains(version)) {
                return State.OUTDATED;
            } else {
                return State.UNKNOWN;
            }
        } else {
            if (entry.matches(c.getLatestVersion()) || Objects.equals('*', entry.getModifier())) {
                return State.UP_TO_DATE;
            }

            String prefix = buildSemverPrefix(entry); // ^1.2.3 == 1.*.*

            // check if it's latest major
            if (c.getLatestVersion().startsWith(prefix)) {
                if (entry.getModifier() != null) { // ~ or ^, as it will always
                                                   // look for newest version
                    return State.UP_TO_DATE;
                } else if (compVersions.contains(version)) {
                    return State.OUTDATED;
                } else {
                    return State.UNKNOWN;
                }
            }

            ComponentVersion latest = getLatestSemanticVersion(c, prefix);

            if (latest != null
                    && new Date(System.currentTimeMillis() - DAY_VAL * majorOffset).before(latest.getPublished())
                    && (entry.getModifier() != null || version.equals(latest.getVersion()))) {
                return State.UP_TO_DATE;
            } else if (compVersions.contains(entry.toCanonicalForm())) {
                return State.OUTDATED;
            }

            return State.UNKNOWN;
        }

    }

    public static String buildSemverPrefix(SemverEntry entry) {
        if (entry.getModifier() != null && entry.getModifier().equals('^')) {
            return entry.getMajor() + ".";
        } else {
            return entry.getMajor() + "." + entry.getMinor() + ".";
        }
    }

    public static SemverEntry parseSemver(String version) {
        if (version == null) {
            return null;
        }

        if ("*".equals(version)) {
            return new SemverEntry(0, 0, 0, '*');
        }

        if (version.startsWith("^") || version.startsWith("~")) {
            SemverEntry entry = parseSemverRegex(version.substring(1));
            if (entry == null) {
                return null;
            }

            entry.setModifier(version.charAt(0));
            return entry;
        }

        return parseSemverRegex(version);
    }

    public static SemverEntry parseSemverRegex(String version) {
        if (version == null) {
            return null;
        }

        Matcher m = SEMVER_PATTERN.matcher(version);
        if (m.matches()) {
            return new SemverEntry(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3)));
        }

        return null;
    }

}
