package me.raska.opendvs.core.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import me.raska.opendvs.base.model.security.ApiToken;
import me.raska.opendvs.core.dto.ApiTokenRepository;
import me.raska.opendvs.core.exception.NotFoundException;

@Service
public class UserService {
    @Autowired
    private ApiTokenRepository apiTokenRepository;

    @Autowired
    private UserSession session;

    public List<ApiToken> getApiTokens() {
        return apiTokenRepository.findByUser(session.getUser());
    }

    public ApiToken createApiToken() {
        ApiToken token = new ApiToken();
        token.setUser(session.getUser());

        return apiTokenRepository.save(token);
    }

    public void deleteApiToken(String id) {
        ApiToken token = apiTokenRepository.findOne(id);
        if (token == null || !token.getUser().getId().equals(session.getUser().getId())) {
            throw new NotFoundException("API Token not found");
        }

        apiTokenRepository.delete(id);
    }
}
