package me.raska.opendvs.core.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import me.raska.opendvs.base.model.security.User;
import me.raska.opendvs.core.service.UserSession;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/users")
public class UserRestController {

    @Autowired
    private UserSession userSession;

    @RequestMapping(value = "/me", method = RequestMethod.GET)
    public User getSelf() {
        return userSession.getUser();
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public void handleLogin(@RequestParam("redirectUrl") String redirectUrl, HttpServletResponse res)
            throws IOException {
        res.sendRedirect(redirectUrl);
        res.flushBuffer();
    }

}
