package me.raska.opendvs.resolver.dto;

import org.springframework.data.jpa.repository.JpaRepository;

import me.raska.opendvs.base.model.Component;

public interface ComponentRepository extends JpaRepository<Component, String> {
    Component findByNameAndGroup(String name, String group);
}
