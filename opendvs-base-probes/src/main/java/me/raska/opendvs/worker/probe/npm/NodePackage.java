package me.raska.opendvs.worker.probe.npm;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NodePackage {
    private String name;
    private String version;
    private Map<String, String> dependencies;
    private Map<String, String> devDependencies;
}
