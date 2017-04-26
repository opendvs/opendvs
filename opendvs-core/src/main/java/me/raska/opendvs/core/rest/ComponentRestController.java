package me.raska.opendvs.core.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import me.raska.opendvs.base.model.Component;
import me.raska.opendvs.core.rest.filtering.Filterable;
import me.raska.opendvs.core.service.ComponentService;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/components")
public class ComponentRestController {

    @Autowired
    private ComponentService componentService;

    @RequestMapping(method = RequestMethod.GET)
    public Page<Component> getComponents(Pageable pageable, Filterable filter) {
        return componentService.getComponents(pageable, filter);
    }

    // use detail endpoint to avoid dots issue
    @RequestMapping(value = "/{id}/detail", method = RequestMethod.GET)
    public Component getComponent(@PathVariable("id") String id) {
        return componentService.getComponent(id);
    }

}
