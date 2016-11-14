package me.raska.opendvs.base.model;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
@Table(name = "COMPONENT", uniqueConstraints = { @UniqueConstraint(columnNames = { "component_group", "name" }) })
public class Component {
    @Id
    private String id;

    /**
     * E.g. maven, jar, exe
     */
    @Column(nullable = false, name = "component_group")
    private String group;

    @Column(nullable = false)
    private String name;

    private String latestVersion;

    @OneToMany(mappedBy = "component")
    private Set<ComponentVersion> versions;
}
