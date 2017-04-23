package me.raska.opendvs.core.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import me.raska.opendvs.base.model.security.User;
import me.raska.opendvs.core.dto.UserRepository;

/**
 * Service used for authentication and authorization purposes. All methods are
 * unsecured and shouldn't be propagated to presentation layer.
 * 
 * @author raskaluk
 *
 */
@Service
public class UserSecurityService {
    @Value("${security.oauth2.claim.identifier:email}")
    private String claimIdentifier;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSession userSession;

    /**
     * 
     * @param claims
     *            obtained from successful OIDC authentication
     * @return GrantedAuthority list
     */
    @Transactional
    public List<GrantedAuthority> generateUserSession(Map<String, Object> claims) {
        if (!claims.containsKey(claimIdentifier)) {
            throw new RuntimeException("User cannot be identifier from claims");
            // TODO: custom exception
        }

        final String id = claims.get(claimIdentifier).toString();
        User user = userRepository.findOne(id);
        if (user == null) {
            user = createUser(id);
        }

        userSession.setUser(user);
        return user.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    private User createUser(String id) {
        User u = new User();
        u.setId(id);
        u.setRoles(new HashSet<>());

        // initial bootstrap
        if (userRepository.count() == 0) {
            u.getRoles().add("ADMIN");
        }

        return userRepository.save(u);
    }

    @Transactional
    public void updateUserAuthorities(String authority) {
        User u = userRepository.findOne(userSession.getUser().getId());
        u.getRoles().add(authority);
        userSession.setUser(userRepository.save(u));
    }
}
