package me.raska.opendvs.core.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import me.raska.opendvs.base.model.Component;
import me.raska.opendvs.core.service.ComponentService;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/components")
public class ComponentRestController {

    @Autowired
    private ComponentService componentService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public Page<Component> getComponents(Pageable pageable) {
        return componentService.getComponents(pageable);
    }

}
