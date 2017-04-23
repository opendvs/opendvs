package me.raska.opendvs.core.service;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.Setter;
import me.raska.opendvs.base.model.security.User;

/**
 * Service used for storing user session data
 * 
 * @author raskaluk
 *
 */
@Service
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Getter
@Setter
public class UserSession {

    private User user;

    public boolean isAdmin() {
        if (user == null) {
            return false;
        }

        return user.getRoles().contains("ADMIN");
    }
}
