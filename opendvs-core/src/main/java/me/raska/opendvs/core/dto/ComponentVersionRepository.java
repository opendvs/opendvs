package me.raska.opendvs.core.dto;

import org.springframework.data.jpa.repository.JpaRepository;

import me.raska.opendvs.base.model.ComponentVersion;

public interface ComponentVersionRepository extends JpaRepository<ComponentVersion, String> {

}
