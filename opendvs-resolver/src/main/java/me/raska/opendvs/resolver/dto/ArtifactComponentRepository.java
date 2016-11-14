package me.raska.opendvs.resolver.dto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import me.raska.opendvs.base.model.artifact.ArtifactComponent;
import me.raska.opendvs.base.model.artifact.ArtifactComponent.State;

public interface ArtifactComponentRepository extends JpaRepository<ArtifactComponent, String> {
    Page<ArtifactComponent> findByNameAndGroupAndStateNot(String name, String group, State state, Pageable page);
}
