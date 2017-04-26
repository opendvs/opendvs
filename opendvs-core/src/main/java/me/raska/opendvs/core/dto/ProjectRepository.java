package me.raska.opendvs.core.dto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import me.raska.opendvs.base.model.project.Project;

public interface ProjectRepository extends JpaRepository<Project, String>, JpaSpecificationExecutor<Project> {
    Page<Project> findByIdIn(Iterable<String> ids, Pageable pageable);
}
