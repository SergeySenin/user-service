package io.github.sergeysenin.userservice.service.user.country;

import io.github.sergeysenin.userservice.entity.user.country.Country;
import io.github.sergeysenin.userservice.exception.type.CountryNotFoundException;
import io.github.sergeysenin.userservice.repository.user.country.CountryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;

    @Transactional(readOnly = true)
    public Country getCountryOrThrow(Long countryId) {
        return countryRepository.findById(countryId)
                .orElseThrow(() -> new CountryNotFoundException("Страна не найдена: id=" + countryId));
    }
}
