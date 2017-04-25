package me.raska.opendvs.core.configuration;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import me.raska.opendvs.base.model.security.ApiToken;
import me.raska.opendvs.base.model.security.User;
import me.raska.opendvs.core.dto.ApiTokenRepository;
import me.raska.opendvs.core.service.UserSession;

@Service
public class ApiTokenInterceptorFilter implements Filter {
    public static final String HEADER = "X-API";

    @Autowired
    private ApiTokenRepository apiTokenRepository;

    @Autowired
    private UserSession userSession;

    @Override
    public void destroy() {
        // nothing to be done
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filter)
            throws IOException, ServletException {
        // don't handle all requests
        if (!isAuthenticated() && req instanceof HttpServletRequest) {
            HttpServletRequest httpReq = (HttpServletRequest) req;

            handleApiToken(httpReq.getHeader(HEADER), httpReq);
        }

        filter.doFilter(req, res);
    }

    private void handleApiToken(String header, HttpServletRequest req) {
        if (header != null) {
            ApiToken token = apiTokenRepository.findOne(header);
            if (token != null) {
                authenticate(token.getUser(), req);
            }
        }
    }

    /**
     * Sets proper
     * 
     * @param user
     * @param req
     */
    private void authenticate(User user, HttpServletRequest req) {
        List<SimpleGrantedAuthority> roles = user.getRoles().stream().map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        UsernamePasswordAuthenticationToken userToken = new UsernamePasswordAuthenticationToken(user.getId(), null,
                roles);
        SecurityContextHolder.getContext().setAuthentication(userToken);

        // http://stackoverflow.com/questions/25440059/how-to-login-a-user-programmatically-using-spring-security/38868001#38868001
        HttpSession session = req.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        // set our session variable
        userSession.setUser(user);
    }

    /**
     * Check if user is authenticated via Spring Security
     * 
     * @return true/false
     */
    private boolean isAuthenticated() {
        return SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // nothing to be done
    }

}
