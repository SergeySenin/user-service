package io.github.sergeysenin.userservice.service.user;

import io.github.sergeysenin.userservice.entity.user.User;
import io.github.sergeysenin.userservice.exception.type.UserNotFoundException;
import io.github.sergeysenin.userservice.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
UserService — слой работы с пользователями, инкапсулирующий поиск и сохранение,
а также выброс UserNotFoundException при отсутствии записи.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getExistingUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден: id=" + userId));
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }
}
