package me.raska.opendvs.core.dto;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import me.raska.opendvs.base.model.security.ApiToken;
import me.raska.opendvs.base.model.security.User;

public interface ApiTokenRepository extends JpaRepository<ApiToken, String> {
    List<ApiToken> findByUser(User user);
}
