package me.raska.opendvs.core.dto;

import org.springframework.data.jpa.repository.JpaRepository;

import me.raska.opendvs.base.model.Component;

public interface ComponentRepository extends JpaRepository<Component, String> {

}
