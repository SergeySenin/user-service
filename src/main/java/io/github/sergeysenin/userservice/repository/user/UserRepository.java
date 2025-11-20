package io.github.sergeysenin.userservice.repository.user;

import io.github.sergeysenin.userservice.entity.user.User;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "country")
    Optional<User> findWithCountryById(Long userId);
}
