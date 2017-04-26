package me.raska.opendvs.core.dto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import me.raska.opendvs.base.model.poller.PollerAction;

public interface PollerActionRepository
        extends JpaRepository<PollerAction, String>, JpaSpecificationExecutor<PollerAction> {
    @Query("select count(a.id) from PollerAction a where a.artifactId = :artifactId and a.state in ('QUEUED','IN_PROGRESS')")
    long getRunningActions(@Param("artifactId") String artifactId);
}
