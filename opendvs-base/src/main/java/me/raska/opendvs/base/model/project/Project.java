package me.raska.opendvs.base.model.project;

import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

import lombok.Getter;
import lombok.Setter;
import me.raska.opendvs.base.model.artifact.Artifact;

@Getter
@Setter
@Entity
@JsonIdentityInfo(generator = JSOGGenerator.class)
public class Project {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    /**
     * Boundary for determining UP_TO_DATE / OUTDATED state (in days) when
     * component is using semantic versioning (http://semver.org/) and major
     * version doesn't equals latest major version
     */
    private int majorVersionOffset;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "project")
    private List<Artifact> artifacts;

    @ElementCollection
    @Lob
    private Map<String, String> typeProperties;

    public Project clone() {
        Project p = new Project();
        p.id = this.id;
        p.name = this.name;
        p.type = this.type;
        p.majorVersionOffset = this.majorVersionOffset;

        return p;
    }
}
