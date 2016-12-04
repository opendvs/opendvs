package me.raska.opendvs.core.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import me.raska.opendvs.base.model.Component;
import me.raska.opendvs.base.model.ComponentVersion;
import me.raska.opendvs.base.model.poller.PollerAction;
import me.raska.opendvs.base.model.poller.PollerActionStep;
import me.raska.opendvs.base.resolver.ResolverAction;
import me.raska.opendvs.core.dto.ComponentRepository;
import me.raska.opendvs.core.dto.ComponentVersionRepository;
import me.raska.opendvs.core.dto.PollerActionRepository;
import me.raska.opendvs.core.dto.PollerActionStepRepository;

@Slf4j
@Service
public class PollerWorkerService {

    @Autowired
    private ComponentRepository componentRepository;

    @Autowired
    private PollerActionRepository pollerActionRepository;

    @Autowired
    private PollerActionStepRepository pollerActionStepRepository;

    @Autowired
    private ComponentVersionRepository componentVersionRepository;

    @Transactional
    public List<ResolverAction> process(PollerAction action) {
        PollerAction act = pollerActionRepository.findOne(action.getId());
        if (act == null) {
            throw new RuntimeException("Cannot find PollerAction " + action.getId() + ", ensure it's not transactional issue!");
        }

        List<ResolverAction> resolverActions = new ArrayList<>();

        if (action.getSteps() != null && !action.getSteps().isEmpty()) {
            for (PollerActionStep step : action.getSteps()) {
                step.setId(null);
                step.setPollerAction(act);

                if (step.getDetectedComponents() != null) {
                    ResolverAction a = storeComponents(step.getDetectedComponents());
                    if (a != null) {
                        resolverActions.add(a);
                    }
                }
            }
            pollerActionStepRepository.save(action.getSteps());
        } else {
            if (action.getState() != act.getState()) {
                act.setState(action.getState());
                act.setStarted(action.getStarted());
                act.setEnded(action.getEnded());
                pollerActionRepository.save(act);
            }
        }

        return resolverActions;
    }

    private ResolverAction storeComponents(Set<Component> components) {
        Set<String> compIds = new HashSet<String>();
        for (Component c : components) {
            if (c.getId() == null) {
                log.warn("Obtained component with null ID: " + c); // TODO:
                                                                   // serialized
                continue;
            }

            Component storedComponent = componentRepository.findOne(c.getId());
            if (storedComponent == null) {
                storedComponent = c;
                compIds.add(storedComponent.getId());
            } else {
                storedComponent.setLatestVersion(c.getLatestVersion());
            }

            Set<ComponentVersion> versions = c.getVersions();
            c.setVersions(null);
            storedComponent = componentRepository.save(storedComponent);

            for (ComponentVersion v : versions) {
                // TODO: full sync option
                if (!componentVersionRepository.exists(v.getId())) {
                    v.setComponent(storedComponent);
                    componentVersionRepository.save(v);

                    compIds.add(storedComponent.getId());
                }
            }
        }

        if (!compIds.isEmpty()) {
            // if any component change occured, resolve all necessary artifacts
            // and update their status
            ResolverAction act = new ResolverAction();
            act.setComponents(compIds);
            return act; // we need to jump out of transactional context!
        }

        return null;
    }
}
