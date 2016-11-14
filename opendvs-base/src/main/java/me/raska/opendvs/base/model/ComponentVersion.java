package me.raska.opendvs.base.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
public class ComponentVersion {
    @Id
    private String id;

    private String version;

    @Column(nullable = false)
    private String source;

    private String hash;

    private Date published;

    @Column(nullable = false)
    private Date synced;

    private String packaging;

    @ManyToOne
    private Component component;
}
