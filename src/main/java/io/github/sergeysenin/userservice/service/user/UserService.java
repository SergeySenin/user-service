package io.github.sergeysenin.userservice.service.user;

import io.github.sergeysenin.userservice.dto.user.CreateUserRequest;
import io.github.sergeysenin.userservice.dto.user.UpdateUserRequest;
import io.github.sergeysenin.userservice.entity.user.User;
import io.github.sergeysenin.userservice.entity.user.country.Country;
import io.github.sergeysenin.userservice.exception.type.UserNotFoundException;
import io.github.sergeysenin.userservice.repository.user.UserRepository;

import io.github.sergeysenin.userservice.service.user.country.CountryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CountryService countryService;

    @Transactional
    public User createUser(CreateUserRequest request) {
        Country country = countryService.getCountryOrThrow(request.countryId());

        var user = User.builder()
                .username(request.username())
                .email(request.email())
                .phone(request.phone())
                .active(request.active())
                .aboutMe(request.aboutMe())
                .country(country)
                .city(request.city())
                .experience(request.experience())
                .userProfileAvatar(null)
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long userId, UpdateUserRequest request) {
        Country country = request.countryId() != null ?
                countryService.getCountryOrThrow(request.countryId()) :
                null;

        var user = getUserByIdOrThrow(userId);
        user.updateProfile(
                request.username(),
                request.email(),
                request.phone(),
                request.active(),
                request.aboutMe(),
                country,
                request.city(),
                request.experience()
        );

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getUserByIdOrThrow(Long userId) {
        return userRepository.findWithCountryById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден: id=" + userId));
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }
}
