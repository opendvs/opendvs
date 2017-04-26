package me.raska.opendvs.core.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import me.raska.opendvs.base.model.poller.PollerAction;
import me.raska.opendvs.base.model.poller.PollerActionStep;
import me.raska.opendvs.core.rest.filtering.Filterable;
import me.raska.opendvs.core.service.PollerService;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/pollers")
public class PollerRestController {

    @Autowired
    private PollerService pollerService;

    @RequestMapping(value = "/actions", method = RequestMethod.GET)
    public Page<PollerAction> getActions(Pageable pageable, Filterable filter) {
        return pollerService.getActions(pageable, filter);
    }

    @RequestMapping(value = "/actions", method = RequestMethod.POST)
    public PollerAction triggerAction(@RequestBody PollerAction action) {
        return pollerService.triggerAction(action);
    }

    @RequestMapping(value = "/action/{id}", method = RequestMethod.GET)
    public PollerAction getAction(@PathVariable("id") String id, Pageable pageable) {
        PollerAction act = pollerService.getAction(id);
        act.setSteps(null);
        return act;
    }

    @RequestMapping(value = "/action/{id}/steps", method = RequestMethod.GET)
    public Page<PollerActionStep> getActionSteps(@PathVariable("id") String id, Pageable pageable, Filterable filter) {
        return pollerService.getActionSteps(id, pageable, filter);
    }

}