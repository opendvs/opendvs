package me.raska.opendvs.core.dto;

import org.springframework.data.jpa.repository.JpaRepository;

import me.raska.opendvs.base.model.security.User;

public interface UserRepository extends JpaRepository<User, String> {

}
