package me.raska.opendvs.resolver.dto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import me.raska.opendvs.base.model.artifact.Artifact;
import me.raska.opendvs.base.model.project.Project;

public interface ArtifactRepository extends JpaRepository<Artifact, String> {
    Page<Artifact> findByProject(Project project, Pageable page);
}
