package io.github.sergeysenin.userservice.service.user;

import io.github.sergeysenin.userservice.entity.user.User;
import io.github.sergeysenin.userservice.entity.user.country.Country;
import io.github.sergeysenin.userservice.exception.type.UserNotFoundException;
import io.github.sergeysenin.userservice.repository.user.UserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("UserService")
class UserServiceTest {

    private static final Long EXISTING_USER_ID = 42L;
    private static final Long MISSING_USER_ID = 4242L;
    private static final String DEFAULT_USERNAME = "john.doe";
    private static final String DEFAULT_EMAIL = "john.doe@example.com";
    private static final String DEFAULT_PHONE = "+70000000000";
    private static final String DEFAULT_PASSWORD = "encoded-password";
    private static final String DEFAULT_ABOUT = "Senior Java developer";
    private static final String DEFAULT_CITY = "Moscow";
    private static final short DEFAULT_EXPERIENCE = 5;
    private static final String DEFAULT_COUNTRY_TITLE = "Russia";
    private static final String NEW_USER_USERNAME = "new.user";
    private static final String NEW_USER_EMAIL = "new.user@example.com";
    private static final String NEW_USER_PHONE = "+71111111111";
    private static final String NEW_USER_PASSWORD = "raw-password";
    private static final String NEW_USER_ABOUT = "Newcomer";
    private static final String NEW_USER_CITY = "Saint-Petersburg";
    private static final short NEW_USER_EXPERIENCE = 1;
    private static final String NEW_USER_COUNTRY_TITLE = "Belarus";

    @Mock
    private UserRepository userRepository;

    @Nested
    @DisplayName("Метод getUserByIdOrThrow")
    class GetUserByIdOrThrowTests {

        @Test
        @DisplayName("должен вернуть пользователя когда пользователь найден")
        void shouldReturnUserWhenUserFound() {
            User expectedUser = userBuilder()
                    .withUsername(DEFAULT_USERNAME)
                    .withEmail(DEFAULT_EMAIL)
                    .withPhone(DEFAULT_PHONE)
                    .withPassword(DEFAULT_PASSWORD)
                    .withActive(true)
                    .withAbout(DEFAULT_ABOUT)
                    .withCountryTitle(DEFAULT_COUNTRY_TITLE)
                    .withCity(DEFAULT_CITY)
                    .withExperience(DEFAULT_EXPERIENCE)
                    .build();
            when(userRepository.findById(EXISTING_USER_ID)).thenReturn(Optional.of(expectedUser));
            UserService sut = createSut();

            User actualUser = sut.getUserByIdOrThrow(EXISTING_USER_ID);

            assertSame(expectedUser, actualUser, "Должен быть возвращён найденный пользователь");
            verify(userRepository).findById(EXISTING_USER_ID);
            verifyNoMoreInteractions(userRepository);
        }

        @Test
        @DisplayName("должен бросить исключение когда пользователь отсутствует")
        void shouldThrowUserNotFoundExceptionWhenUserAbsent() {
            when(userRepository.findById(MISSING_USER_ID)).thenReturn(Optional.empty());
            UserService sut = createSut();

            UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                    () -> sut.getUserByIdOrThrow(MISSING_USER_ID),
                    "Ожидалось исключение, если пользователь не найден");

            assertEquals("Пользователь не найден: id=" + MISSING_USER_ID, exception.getMessage(),
                    "Сообщение должно содержать идентификатор отсутствующего пользователя");
            verify(userRepository).findById(MISSING_USER_ID);
            verifyNoMoreInteractions(userRepository);
        }
    }

    @Nested
    @DisplayName("Метод save")
    class SaveUserTests {

        @Test
        @DisplayName("должен сохранить пользователя")
        void shouldReturnSavedUserWhenSaveInvoked() {
            User userToSave = userBuilder()
                    .withUsername(NEW_USER_USERNAME)
                    .withEmail(NEW_USER_EMAIL)
                    .withPhone(NEW_USER_PHONE)
                    .withPassword(NEW_USER_PASSWORD)
                    .withActive(null)
                    .withAbout(NEW_USER_ABOUT)
                    .withCountryTitle(NEW_USER_COUNTRY_TITLE)
                    .withCity(NEW_USER_CITY)
                    .withExperience(NEW_USER_EXPERIENCE)
                    .build();
            User savedUser = userBuilder()
                    .withUsername(NEW_USER_USERNAME)
                    .withEmail(NEW_USER_EMAIL)
                    .withPhone(NEW_USER_PHONE)
                    .withPassword(NEW_USER_PASSWORD)
                    .withActive(true)
                    .withAbout(NEW_USER_ABOUT)
                    .withCountryTitle(NEW_USER_COUNTRY_TITLE)
                    .withCity(NEW_USER_CITY)
                    .withExperience(NEW_USER_EXPERIENCE)
                    .build();
            when(userRepository.save(userToSave)).thenReturn(savedUser);
            UserService sut = createSut();

            User actualUser = sut.save(userToSave);

            assertSame(savedUser, actualUser, "Должен быть возвращён сохранённый пользователь");
            verify(userRepository).save(userToSave);
            verifyNoMoreInteractions(userRepository);
        }
    }

    private UserService createSut() {
        return new UserService(userRepository);
    }

    private static UserTestBuilder userBuilder() {
        return new UserTestBuilder();
    }

    private static final class UserTestBuilder {

        private String username;
        private String email;
        private String phone;
        private String password;
        private Boolean active;
        private String about;
        private String countryTitle;
        private String city;
        private Short experience;

        private UserTestBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        private UserTestBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        private UserTestBuilder withPhone(String phone) {
            this.phone = phone;
            return this;
        }

        private UserTestBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        private UserTestBuilder withActive(Boolean active) {
            this.active = active;
            return this;
        }

        private UserTestBuilder withAbout(String about) {
            this.about = about;
            return this;
        }

        private UserTestBuilder withCountryTitle(String countryTitle) {
            this.countryTitle = countryTitle;
            return this;
        }

        private UserTestBuilder withCity(String city) {
            this.city = city;
            return this;
        }

        private UserTestBuilder withExperience(short experience) {
            this.experience = experience;
            return this;
        }

        private User build() {
            Country country = countryTitle == null ? null : Country.builder().title(countryTitle).build();
            return User.builder()
                    .username(username)
                    .email(email)
                    .phone(phone)
                    .password(password)
                    .active(active)
                    .aboutMe(about)
                    .country(country)
                    .city(city)
                    .experience(experience)
                    .userProfileAvatar(null)
                    .build();
        }
    }
}
