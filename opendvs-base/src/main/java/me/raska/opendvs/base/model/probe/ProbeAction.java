package me.raska.opendvs.base.model.probe;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Getter;
import lombok.Setter;
import me.raska.opendvs.base.model.artifact.Artifact;

@Getter
@Setter
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
public class ProbeAction {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private Artifact artifact;

    private Date initiated;
    private Date started;
    private Date ended;

    @Enumerated(EnumType.STRING)
    private State state;
    private long maxIterations;

    /**
     * Display also steps that have zero output
     */
    private boolean debug;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "probeAction")
    private List<ProbeActionStep> steps;

    public static enum State {
        QUEUED, IN_PROGRESS, SUCCESS, FAILURE;
    }
}
