package io.github.sergeysenin.userservice.repository.user;

import io.github.sergeysenin.userservice.entity.user.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {}
