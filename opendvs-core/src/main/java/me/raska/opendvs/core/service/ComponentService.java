package me.raska.opendvs.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import me.raska.opendvs.base.model.Component;
import me.raska.opendvs.core.dto.ComponentRepository;

@Service
public class ComponentService {
    @Autowired
    private ComponentRepository componentRepository;

    public Page<Component> getComponents(Pageable page) {
        return componentRepository.findAll(page);
    }

}
