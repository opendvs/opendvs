package me.raska.opendvs.resolver.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SemverEntry {
    private final int major;
    private final int minor;
    private final int patch;

    @Setter
    private Character modifier;

    public String toCanonicalForm() {
        return major + "." + minor + "." + patch;
    }

    public boolean matches(String version) {
        return toCanonicalForm().equals(version);
    }
}
