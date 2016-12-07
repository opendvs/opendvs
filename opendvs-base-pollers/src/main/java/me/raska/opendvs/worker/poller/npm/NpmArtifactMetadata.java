package me.raska.opendvs.worker.poller.npm;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class NpmArtifactMetadata {
    private String name;

    @JsonProperty("dist-tags")
    private Map<String, String> tags;

    private Map<String, NpmArtifactVersion> versions;

    private Map<String, Object> time;

}
