package io.github.sergeysenin.userservice.repository.user.country;

import io.github.sergeysenin.userservice.entity.user.country.Country;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
}
