package me.raska.opendvs.core.dto;

import org.springframework.data.jpa.repository.JpaRepository;

import me.raska.opendvs.base.model.artifact.ArtifactComponent;

public interface ArtifactComponentRepository extends JpaRepository<ArtifactComponent, String> {

}
