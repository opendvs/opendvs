package me.raska.opendvs.base.model.project;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ProjectType {
    private final String id;
    private final String name;
    private final String description;

    private List<Property> properties;

    @Builder
    @Data
    public static class Property {
        private final String key;
        private final String name;
        private final String description;

        private final FieldType type;
    }

    public enum FieldType {
        TEXT, TEXTAREA
    }
}
