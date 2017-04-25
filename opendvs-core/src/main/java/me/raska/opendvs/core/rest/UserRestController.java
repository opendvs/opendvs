package me.raska.opendvs.core.rest;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import me.raska.opendvs.base.model.security.ApiToken;
import me.raska.opendvs.base.model.security.User;
import me.raska.opendvs.core.service.UserService;
import me.raska.opendvs.core.service.UserSession;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/users")
public class UserRestController {

    @Autowired
    private UserSession userSession;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/me", method = RequestMethod.GET)
    public User getSelf() {
        return userSession.getUser();
    }

    @RequestMapping(value = "/me/tokens", method = RequestMethod.GET)
    public List<ApiToken> getUserTokens() {
        return userService.getApiTokens();
    }

    @RequestMapping(value = "/me/tokens", method = RequestMethod.POST)
    public ApiToken createApiToken() {
        return userService.createApiToken();
    }

    @RequestMapping(value = "/me/tokens/{id}", method = RequestMethod.DELETE)
    public void deleteApiToken(@PathVariable("id") String token) {
        userService.deleteApiToken(token);
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public void handleLogin(@RequestParam("redirectUrl") String redirectUrl, HttpServletResponse res)
            throws IOException {
        res.sendRedirect(redirectUrl);
        res.flushBuffer();
    }

}
