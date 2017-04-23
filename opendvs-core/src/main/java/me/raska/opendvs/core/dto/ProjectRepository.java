package me.raska.opendvs.core.dto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import me.raska.opendvs.base.model.project.Project;

public interface ProjectRepository extends JpaRepository<Project, String> {
    Page<Project> findByIdIn(Iterable<String> ids, Pageable pageable);
}
