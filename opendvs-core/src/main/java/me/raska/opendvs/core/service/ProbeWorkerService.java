package me.raska.opendvs.core.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import me.raska.opendvs.base.model.artifact.Artifact;
import me.raska.opendvs.base.model.artifact.ArtifactComponent;
import me.raska.opendvs.base.model.probe.ProbeAction;
import me.raska.opendvs.base.model.probe.ProbeActionStep;
import me.raska.opendvs.base.resolver.ResolverAction;
import me.raska.opendvs.base.resolver.amqp.ResolverRabbitService;
import me.raska.opendvs.core.dto.ArtifactComponentRepository;
import me.raska.opendvs.core.dto.ArtifactRepository;
import me.raska.opendvs.core.dto.ProbeActionRepository;
import me.raska.opendvs.core.dto.ProbeActionStepRepository;

@Service
public class ProbeWorkerService {

    @Autowired
    private ArtifactRepository artifactRepository;

    @Autowired
    private ProbeActionRepository probeActionRepository;

    @Autowired
    private ProbeActionStepRepository probeActionStepRepository;

    @Autowired
    private ArtifactComponentRepository artifactComponentRepository;

    @Autowired
    @Qualifier(ResolverRabbitService.RESOLVER_QUALIFIER)
    private RabbitTemplate resolverTemplate;

    @Transactional
    public ResolverAction process(ProbeAction action) {

        ProbeAction act = probeActionRepository.findOne(action.getId());
        Artifact art = artifactRepository.findOne(act.getArtifact().getId());
        act.setEnded(action.getEnded());
        act.setStarted(action.getStarted());
        act.setState(action.getState());

        Set<ArtifactComponent> components = new HashSet<>();
        List<ProbeActionStep> steps = new ArrayList<>();

        if (action.getSteps() != null) {
            action.getSteps().stream().forEach(s -> {
                s.setId(null);
                s.setProbeAction(act);

                Set<ArtifactComponent> cs = s.getDetectedComponents();
                s.setDetectedComponents(null);
                ProbeActionStep st = probeActionStepRepository.save(s);
                steps.add(st);

                if (cs != null) {
                    cs.stream().forEach(c -> {
                        c.setId(null);
                        c.setArtifact(art);
                        c.setProbeActionStep(st);
                        c.setState(ArtifactComponent.State.UNKNOWN);
                        art.getComponents().add(c);
                    });
                    components.addAll(cs);
                }

            });
        }
        act.setSteps(steps);

        probeActionRepository.save(act);
        artifactComponentRepository.save(components);

        // since artifact change occured
        ResolverAction resact = new ResolverAction();
        Set<String> set = new HashSet<>();
        set.add(art.getId());
        resact.setArtifacts(set);
        return resact; // jump out of transactional context

    }
}
