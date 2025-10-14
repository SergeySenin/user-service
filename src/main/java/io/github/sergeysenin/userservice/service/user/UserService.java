package io.github.sergeysenin.userservice.service.user;

import io.github.sergeysenin.userservice.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/*
UserService — слой работы с пользователями, инкапсулирующий поиск и сохранение,
а также выброс UserNotFoundException при отсутствии записи.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // Необходимые для работы методы
}
