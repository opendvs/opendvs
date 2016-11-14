package me.raska.opendvs.core.dto;

import org.springframework.data.jpa.repository.JpaRepository;

import me.raska.opendvs.base.model.poller.PollerAction;

public interface PollerActionRepository extends JpaRepository<PollerAction, String> {

}
