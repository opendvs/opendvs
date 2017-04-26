package me.raska.opendvs.core.dto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import me.raska.opendvs.base.model.poller.PollerAction;
import me.raska.opendvs.base.model.poller.PollerActionStep;

public interface PollerActionStepRepository extends JpaRepository<PollerActionStep, String>, JpaSpecificationExecutor<PollerActionStep> {
    Page<PollerActionStep> findByPollerAction(PollerAction action, Pageable page);
}
