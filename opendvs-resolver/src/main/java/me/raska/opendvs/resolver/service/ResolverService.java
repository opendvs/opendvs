package me.raska.opendvs.resolver.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import me.raska.opendvs.base.core.amqp.CoreRabbitService;
import me.raska.opendvs.base.model.Component;
import me.raska.opendvs.base.model.artifact.Artifact;
import me.raska.opendvs.base.model.artifact.ArtifactComponent;
import me.raska.opendvs.base.resolver.ResolverAction;
import me.raska.opendvs.resolver.dto.ArtifactComponentRepository;
import me.raska.opendvs.resolver.dto.ArtifactRepository;
import me.raska.opendvs.resolver.dto.ComponentRepository;

@Slf4j
@Service
public class ResolverService {
    @Autowired
    private ArtifactComponentRepository artifactComponentRepository;

    @Autowired
    private ComponentRepository componentRepository;

    @Autowired
    private ArtifactRepository artifactRepository;

    @Autowired
    @Qualifier(CoreRabbitService.FANOUT_QUALIFIER)
    private RabbitTemplate fanoutTemplate;

    @Value("${opendvs.resolver.component.page_size:10}")
    private int pageSize;

    public void handleMessage(ResolverAction action) {
        assert action != null : "Action cannot be null";

        if (action.getComponents() != null) {
            if (log.isDebugEnabled()) {
                log.debug("Obtained " + action.getComponents().size() + " components to handle");
            }

            action.getComponents().forEach(this::handleInputComponent);
        }

        if (action.getArtifacts() != null) {
            if (log.isDebugEnabled()) {
                log.debug("Obtained " + action.getArtifacts().size() + " artifacts to handle");
            }

            action.getArtifacts().forEach(this::handleInputArtifact);
        }
    }

    @Transactional
    private void handleInputArtifact(String artifact) {
        Artifact art = artifactRepository.findOne(artifact);
        if (art == null) {
            log.warn("Obtained non-existing artifact to resolve with id " + artifact);
            return;
        }

        List<ArtifactComponent> batchUpdate = new ArrayList<>(art.getComponents().size());

        for (ArtifactComponent component : art.getComponents()) {
            Component c = componentRepository.findByNameAndGroup(component.getName(), component.getGroup());
            if (c == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Artifact (" + artifact + ") component " + component.getName() + " in group "
                            + component.getGroup() + " cannot be found");
                }

                continue;
            }
            Set<String> compVersions = c.getVersions().stream().map(cv -> cv.getVersion()).collect(Collectors.toSet());

            if (component.getState() != ArtifactComponent.State.UP_TO_DATE
                    && component.getVersion().equals(c.getLatestVersion())) {
                if (log.isDebugEnabled()) {
                    log.debug("Artifact (" + artifact + ") component " + component.getId() + " is up to date");
                }

                component.setState(ArtifactComponent.State.UP_TO_DATE);
                batchUpdate.add(component);
            } else if (component.getState() != ArtifactComponent.State.OUTDATED
                    && compVersions.contains(component.getVersion())) {
                if (log.isDebugEnabled()) {
                    log.debug("Artifact (" + artifact + ") component " + component.getId() + " is outdated");
                }

                component.setState(ArtifactComponent.State.OUTDATED);
                batchUpdate.add(component);
            } else if (log.isDebugEnabled()) {
                log.debug("Artifact (" + artifact + ") component " + component.getId() + " version "
                        + component.getVersion() + " couldn't be found in known versions " + compVersions);
            }
        }

        if (!batchUpdate.isEmpty()) {
            artifactComponentRepository.save(batchUpdate);
            fanoutTemplate.convertAndSend(batchUpdate);
        }
    }

    private void handleInputComponent(String component) {
        Component comp = componentRepository.findOne(component);
        if (comp == null) {
            log.warn("Obtained non-existing component to resolve with id " + component);
            return;
        }

        Pageable page = new PageRequest(0, pageSize);

        Set<String> compVersions = comp.getVersions().stream().map(cv -> cv.getVersion()).collect(Collectors.toSet());

        boolean run = true;
        while (run) {
            // due to transactions
            page = handlePageRequest(comp, compVersions, page);
            if (page == null) {
                run = false;
            }
        }
    }

    /**
     * Transactional interface to safely handle ArtifactComponent State
     * 
     * @param comp
     * @param compVersions
     * @param page
     * @return new page or null
     */
    @Transactional
    private Pageable handlePageRequest(Component comp, Set<String> compVersions, Pageable page) {
        // vulneable is always vulnerable
        Page<ArtifactComponent> acomps = artifactComponentRepository.findByNameAndGroupAndStateNot(comp.getName(),
                comp.getGroup(), ArtifactComponent.State.VULNERABLE, page);

        List<ArtifactComponent> batchUpdate = new ArrayList<>(pageSize);
        for (ArtifactComponent c : acomps) {
            if (log.isDebugEnabled()) {
                log.debug("Determining state of component " + c.getId() + " due to component " + comp.getId());
            }
            if (c.getState() != ArtifactComponent.State.UP_TO_DATE && c.getVersion() != null && c.getVersion().equals(comp.getLatestVersion())) {
                if (log.isDebugEnabled()) {
                    log.debug("Component " + c.getId() + " is up to date");
                }

                c.setState(ArtifactComponent.State.UP_TO_DATE);
                batchUpdate.add(c);
            } else if (c.getState() != ArtifactComponent.State.OUTDATED && compVersions.contains(c.getVersion())) {
                if (log.isDebugEnabled()) {
                    log.debug("Component " + c.getId() + " is outdated");
                }

                c.setState(ArtifactComponent.State.OUTDATED);
                batchUpdate.add(c);
            } else if (log.isDebugEnabled()) {
                log.debug("Component " + c.getId() + " version " + c.getVersion() + " couldn't be found in known versions " + compVersions);
            }
        }

        // update
        if (!batchUpdate.isEmpty()) {
            artifactComponentRepository.save(batchUpdate);
            fanoutTemplate.convertAndSend(batchUpdate);
        }

        return acomps.nextPageable();
    }
}
