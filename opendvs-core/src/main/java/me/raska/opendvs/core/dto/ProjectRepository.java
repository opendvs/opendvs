package me.raska.opendvs.core.dto;

import org.springframework.data.jpa.repository.JpaRepository;

import me.raska.opendvs.base.model.project.Project;

public interface ProjectRepository extends JpaRepository<Project, String> {

}
