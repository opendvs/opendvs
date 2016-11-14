package me.raska.opendvs.base.model.probe;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Getter;
import lombok.Setter;
import me.raska.opendvs.base.model.artifact.ArtifactComponent;

@Getter
@Setter
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
public class ProbeActionStep {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Lob
    private String output;

    @Enumerated(EnumType.STRING)
    private Type type;

    @Enumerated(EnumType.STRING)
    private State state;

    // to avoid duplicates
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "probeActionStep")
    private Set<ArtifactComponent> detectedComponents;

    @ManyToOne(optional = false)
    private ProbeAction probeAction;

    public static enum State {
        SUCCESS, FAILURE;
    }

    public static enum Type {
        EXTRACTION, DETECTION;
    }
}
