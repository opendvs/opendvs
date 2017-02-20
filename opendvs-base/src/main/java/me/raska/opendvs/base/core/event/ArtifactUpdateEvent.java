package me.raska.opendvs.base.core.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.raska.opendvs.base.core.FanoutEvent;
import me.raska.opendvs.base.model.artifact.Artifact;
import me.raska.opendvs.base.model.project.Project;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArtifactUpdateEvent extends FanoutEvent {
    private Artifact artifact;
    private Project project;
}
