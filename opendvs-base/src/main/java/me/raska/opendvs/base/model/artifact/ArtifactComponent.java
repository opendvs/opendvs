package me.raska.opendvs.base.model.artifact;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Getter;
import lombok.Setter;
import me.raska.opendvs.base.model.probe.ProbeActionStep;

@Getter
@Setter
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
@Table(name = "ARTIFACT_COMPONENT", indexes = { @Index(columnList = "uid") })
public class ArtifactComponent {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    /**
     * E.g. maven:me.raska.opendvs:opendvs-agent
     */
    @Column(nullable = false)
    private String uid;

    /**
     * E.g. maven, jar, exe
     */
    @Column(nullable = false, name = "artifact_group")
    private String group;

    @Column(nullable = false)
    private String name;
    private String version;
    private String scope;

    @Column(name = "artifact_hash")
    private String hash;

    private String parentUid;

    @ManyToOne(optional = false)
    private Artifact artifact;

    @ManyToOne(optional = false)
    private ProbeActionStep probeActionStep;

    private State state;

    public static enum State {
        UNKNOWN, UP_TO_DATE, OUTDATED, VULNERABLE;
    }
}
