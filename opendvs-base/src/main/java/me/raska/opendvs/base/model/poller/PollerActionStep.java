package me.raska.opendvs.base.model.poller;

import java.util.Date;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

import lombok.Getter;
import lombok.Setter;
import me.raska.opendvs.base.model.Component;
import me.raska.opendvs.base.model.Vulnerability;

@Getter
@Setter
@Entity
@JsonIdentityInfo(generator = JSOGGenerator.class)
public class PollerActionStep {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Lob
    private String output;

    private String poller;

    private Date started;

    private Date ended;

    @Enumerated(EnumType.STRING)
    private Type type;

    @Enumerated(EnumType.STRING)
    private State state;

    @Transient
    private Set<Component> detectedComponents;

    @Transient
    private Set<Vulnerability> detectedVulnerabilities;

    @ManyToOne(optional = false)
    private PollerAction pollerAction;

    public static enum State {
        SUCCESS, FAILURE;
    }

    public static enum Type {
        PACKAGE, VULNERABILITY;
    }
}
