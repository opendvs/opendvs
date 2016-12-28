package me.raska.opendvs.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ProjectAsyncService {

    @Autowired
    private ProjectService projectService;

    @Scheduled(fixedRate = 60 * 1000)
    private void resolveArtifactsState() {
        projectService.resolveArtifactsState();
    }
}
