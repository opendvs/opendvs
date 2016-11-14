package me.raska.opendvs.base.probe;

import java.util.List;

import me.raska.opendvs.base.model.artifact.Artifact;
import me.raska.opendvs.base.model.artifact.ArtifactComponent;
import me.raska.opendvs.base.model.probe.ProbeActionStep;

public interface NativeProbe {

    /**
     * Detects resources that should be extracted and does so.
     *
     * @param artifact
     * @param extractedComponents
     * @param context
     * @return list of all executed steps
     */
    List<ProbeActionStep> extract(Artifact artifact, List<ArtifactComponent> extractedComponents,
            ProbingContext context);

    /**
     * Detects components in artifact. Is called before extract method.
     * Extracted artifacts will get passed in next iteration
     * 
     * @param artifact
     * @param extractedComponents
     * @param context
     * @return
     */
    List<ProbeActionStep> detectComponents(Artifact artifact, List<ArtifactComponent> extractedComponents,
            ProbingContext context);
}
