package me.raska.opendvs.base.core.event;

import java.util.Collection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.raska.opendvs.base.core.FanoutEvent;
import me.raska.opendvs.base.model.artifact.Artifact;
import me.raska.opendvs.base.model.artifact.ArtifactComponent;
import me.raska.opendvs.base.model.project.Project;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComponentsResolvedEvent extends FanoutEvent {
    private Collection<ArtifactComponent> components;
    private Artifact artifact;
    private Project project;
}
