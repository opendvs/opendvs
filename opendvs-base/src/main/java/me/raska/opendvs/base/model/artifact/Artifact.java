package me.raska.opendvs.base.model.artifact;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Getter;
import lombok.Setter;
import me.raska.opendvs.base.model.probe.ProbeAction;
import me.raska.opendvs.base.model.project.Project;

@Getter
@Setter
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
@Entity
@Table(name = "ARTIFACT", uniqueConstraints = { @UniqueConstraint(columnNames = { "project_id", "identity" }) })
public class Artifact {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column(nullable = false)
    private String uri;

    @Column(nullable = false)
    private String name;

    /**
     * Identity for the artifact, has to be unique for the project. Could be
     * Hash / Git ref for example
     */
    @Column(nullable = false)
    private String identity;

    @Column(nullable = false)
    private String sourceType;

    @Enumerated(EnumType.STRING)
    private Type type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @OneToOne(fetch = FetchType.LAZY, optional = true, mappedBy = "artifact")
    private ProbeAction probeAction;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "artifact")
    private Set<ArtifactComponent> components;

    public static enum Type {
        build, source
    }
}
