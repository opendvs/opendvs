package me.raska.opendvs.worker.poller.npm;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class NpmArtifactVersion {
    private String version;

    @JsonProperty("_shasum")
    private String shasum;
}
