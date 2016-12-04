package me.raska.opendvs.core.dto;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import me.raska.opendvs.base.model.artifact.Artifact;
import me.raska.opendvs.base.model.artifact.Artifact.State;
import me.raska.opendvs.base.model.project.Project;

public interface ArtifactRepository extends JpaRepository<Artifact, String> {
    Page<Artifact> findByProjectOrderByInitiatedDesc(Project project, Pageable page);

    List<Artifact> findByState(State state);
}
