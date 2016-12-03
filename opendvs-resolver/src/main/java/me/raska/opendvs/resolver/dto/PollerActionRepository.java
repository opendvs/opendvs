package me.raska.opendvs.resolver.dto;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import me.raska.opendvs.base.model.poller.PollerAction;
import me.raska.opendvs.base.model.poller.PollerAction.State;

public interface PollerActionRepository extends JpaRepository<PollerAction, String> {
    List<PollerAction> findByFilterAndStateIn(String filter, List<State> states);
}
