package me.raska.opendvs.core.dto;

import org.springframework.data.jpa.repository.JpaRepository;

import me.raska.opendvs.base.model.probe.ProbeActionStep;

public interface ProbeActionStepRepository extends JpaRepository<ProbeActionStep, String> {

}
