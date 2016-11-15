package me.raska.opendvs.base.model.poller;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@JsonIdentityInfo(generator = JSOGGenerator.class)
public class PollerAction {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private Date initiated;
    private Date started;
    private Date ended;

    private String filter;

    @Enumerated(EnumType.STRING)
    private State state;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pollerAction")
    private List<PollerActionStep> steps;

    public static enum State {
        QUEUED, IN_PROGRESS, SUCCESS, FAILURE;
    }
}
