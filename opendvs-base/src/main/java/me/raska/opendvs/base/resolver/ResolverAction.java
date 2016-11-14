package me.raska.opendvs.base.resolver;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResolverAction {
    private Set<String> components;
    private Set<String> artifacts;
}
