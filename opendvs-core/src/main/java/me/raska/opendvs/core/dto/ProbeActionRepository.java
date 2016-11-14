package me.raska.opendvs.core.dto;

import org.springframework.data.jpa.repository.JpaRepository;

import me.raska.opendvs.base.model.probe.ProbeAction;

public interface ProbeActionRepository extends JpaRepository<ProbeAction, String> {

}
