package me.raska.opendvs.resolver.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import me.raska.opendvs.base.model.ComponentVersion;
import me.raska.opendvs.base.model.artifact.Artifact;
import me.raska.opendvs.base.model.artifact.ArtifactComponent;
import me.raska.opendvs.base.model.poller.PollerAction;
import me.raska.opendvs.base.resolver.ResolverAction;
import me.raska.opendvs.resolver.dto.ArtifactComponentRepository;
import me.raska.opendvs.resolver.dto.ArtifactRepository;
import me.raska.opendvs.resolver.dto.ComponentRepository;
import me.raska.opendvs.resolver.dto.PollerActionRepository;

@Slf4j
@Service
public class ResolverService {
    public static final Pattern SEMVER_PATTERN = Pattern.compile("^([\\d]+)\\.([\\d]+)\\.[\\d]+$");

    public static final int DAY_VAL = 1000 * 60 * 24;

    @Autowired
    private ArtifactComponentRepository artifactComponentRepository;

    @Autowired
    private ComponentRepository componentRepository;

    @Autowired
    private ArtifactRepository artifactRepository;

    @Autowired
    private PollerActionRepository pollerActionRepository;

    @Autowired
    @Qualifier(CoreRabbitService.FANOUT_QUALIFIER)
    private RabbitTemplate fanoutTemplate;

    @Value("${opendvs.resolver.component.page_size:10}")
    private int pageSize;

    @Transactional
    public Map<String, List<ArtifactComponent>> handleMessage(ResolverAction action) {
        assert action != null : "Action cannot be null";
        Map<String, List<ArtifactComponent>> rescanningComponents = new HashMap<>();

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

            for (String a : action.getArtifacts()) {
                rescanningComponents.put(a, handleInputArtifact(a)); // TODO
            }
        }

        return rescanningComponents;
    }

    private List<ArtifactComponent> handleInputArtifact(String artifact) {
        Artifact art = artifactRepository.findOne(artifact);
        if (art == null) {
            throw new RuntimeException("Obtained non-existing artifact to resolve with id " + artifact);
        }

        List<ArtifactComponent> batchUpdate = new ArrayList<>(art.getComponents().size());
        List<ArtifactComponent> rescanningComponents = new ArrayList<>(art.getComponents().size());

        for (ArtifactComponent component : art.getComponents()) {
            if (component.getVersion() == null || component.getVersion().isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("Artifact (" + artifact + ") component " + component.getName() + " in group "
                            + component.getGroup() + " has empty version!");
                }

                continue;
            }

            Component c = componentRepository.findByNameAndGroup(component.getName(), component.getGroup());
            if (c == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Artifact (" + artifact + ") component " + component.getName() + " in group "
                            + component.getGroup() + " cannot be found");
                }

                rescanningComponents.add(component);
                continue;
            }

            Set<String> compVersions = c.getVersions().stream().map(cv -> cv.getVersion()).collect(Collectors.toSet());

            SemverState semver = handleSemver(c, component, art.getProject().getMajorVersionOffset());

            if (semver == SemverState.OK || (component.getState() != ArtifactComponent.State.UP_TO_DATE
                    && component.getVersion() != null && component.getVersion().equals(c.getLatestVersion()))) {
                if (log.isDebugEnabled()) {
                    log.debug("Artifact (" + artifact + ") component " + component.getId() + " is up to date");
                }

                component.setState(ArtifactComponent.State.UP_TO_DATE);
                batchUpdate.add(component);
                rescanningComponents.add(component); // TODO: allow grace period
                                                     // to be configured
            } else if (semver == SemverState.NOK
                    || component.getVersion() != null && component.getState() != ArtifactComponent.State.OUTDATED
                            && compVersions.contains(component.getVersion())) {

                if (log.isDebugEnabled()) {
                    log.debug("Artifact (" + artifact + ") component " + component.getId() + " is outdated");
                }

                component.setState(ArtifactComponent.State.OUTDATED);
                batchUpdate.add(component);
            } else if (log.isDebugEnabled()) {
                log.debug("Artifact (" + artifact + ") component " + component.getId() + " version "
                        + component.getVersion() + " couldn't be found in known versions " + compVersions);
                component.setState(ArtifactComponent.State.UNKNOWN);
                rescanningComponents.add(component);
            }
        }

        if (!batchUpdate.isEmpty()) {
            artifactComponentRepository.save(batchUpdate);
            fanoutTemplate.convertAndSend(batchUpdate);
        }

        return rescanningComponents;
    }

    @Transactional
    public List<PollerAction> publishScanning(Map<String, List<ArtifactComponent>> components) {
        List<PollerAction> actions = new ArrayList<>();
        for (Entry<String, List<ArtifactComponent>> entry : components.entrySet()) {
            for (ArtifactComponent c : entry.getValue()) {
                PollerAction action = new PollerAction();
                action.setFilter(c.getGroup() + ":" + c.getName());
                if (pollerActionRepository.findByFilterAndStateIn(action.getFilter(),
                        Arrays.asList(PollerAction.State.QUEUED, PollerAction.State.IN_PROGRESS)).size() > 0) {
                    continue;
                }

                action.setInitiated(new Date());
                action.setState(PollerAction.State.QUEUED);
                action.setArtifactId(entry.getKey());
                actions.add(pollerActionRepository.save(action));
            }
        }
        return actions;
    }

    private SemverState handleSemver(Component c, ArtifactComponent ac, int majorOffset) {
        if (ac.getVersion().equals("*")) {
            return SemverState.OK;
        }

        Boolean fixMajor = null; // fix for major version
        String version = ac.getVersion();

        if (ac.getVersion().startsWith("^") || ac.getVersion().startsWith("~")) { // strip
                                                                                  // special
                                                                                  // handling
            version = ac.getVersion().substring(1);
            fixMajor = ac.getVersion().startsWith("^");
        }

        Matcher m = SEMVER_PATTERN.matcher(version);
        if (m.matches()) {
            int major = Integer.parseInt(m.group(1));
            int minor = Integer.parseInt(m.group(2));

            String versionStr;
            if (fixMajor == null || fixMajor) {
                versionStr = major + ".";
            } else {
                versionStr = major + "." + minor + ".";
            }

            if (fixMajor == null && majorOffset > 0 && c.getLatestVersion() != null
                    && !c.getLatestVersion().startsWith(versionStr)) {
                // find those who start with the same major version and filter
                // unrelevant ones
                ComponentVersion cv = c.getVersions().stream().filter(v -> v.getVersion().startsWith(versionStr))
                        .filter(v -> v.getPublished() != null && SEMVER_PATTERN.matcher(v.getVersion()).matches())
                        .sorted((v1, v2) -> v2.getPublished().compareTo(v1.getPublished())).findFirst().orElse(null);
                if (cv != null && cv.getPublished() != null
                        && new Date(System.currentTimeMillis() - DAY_VAL * majorOffset).before(cv.getPublished())) {
                    return SemverState.OK;
                }
            } else if (fixMajor != null) {
                // match against latest version
                if (c.getLatestVersion().startsWith(versionStr)) {
                    return SemverState.OK;
                }
                return SemverState.NOK;
            }
        }

        return SemverState.NOT_SEMVER;
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
            if (c.getState() != ArtifactComponent.State.UP_TO_DATE && c.getVersion() != null
                    && c.getVersion().equals(comp.getLatestVersion())) {
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
                log.debug("Component " + c.getId() + " version " + c.getVersion()
                        + " couldn't be found in known versions " + compVersions);
            }
        }

        // update
        if (!batchUpdate.isEmpty()) {
            artifactComponentRepository.save(batchUpdate);
            fanoutTemplate.convertAndSend(batchUpdate);
        }

        return acomps.nextPageable();
    }

    private enum SemverState {
        OK, NOK, NOT_SEMVER;
    }
}
