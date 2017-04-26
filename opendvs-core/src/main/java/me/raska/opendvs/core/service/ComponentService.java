package me.raska.opendvs.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import me.raska.opendvs.base.model.Component;
import me.raska.opendvs.core.dto.ComponentRepository;
import me.raska.opendvs.core.rest.filtering.Filterable;

@Service
public class ComponentService {
    @Autowired
    private ComponentRepository componentRepository;

    public Page<Component> getComponents(Pageable page, Filterable filter) {
        // TODO: handle filterable
        return componentRepository.findAll(page);
    }

    public Component getComponent(String id) {
        return componentRepository.findOne(id);
    }
}
