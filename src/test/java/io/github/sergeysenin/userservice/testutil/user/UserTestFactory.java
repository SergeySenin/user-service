package io.github.sergeysenin.userservice.testutil.user;

import io.github.sergeysenin.userservice.entity.user.User;
import io.github.sergeysenin.userservice.entity.user.UserProfileAvatar;
import io.github.sergeysenin.userservice.entity.user.country.Country;

/**
 * Утильные методы и билдеры для подготовки тестовых сущностей пользователя.
 */
public final class UserTestFactory {

    private UserTestFactory() {
    }

    // Создаёт пользователя с базовыми значениями и переданным аватаром.
    public static User createDefaultUser(UserProfileAvatar avatar) {
        return userBuilder()
                .withUsername("john.doe")
                .withEmail("john.doe@example.com")
                .withPhone("+70000000000")
                .withPassword("encoded")
                .withActive(true)
                .withCountryTitle("Russia")
                .withAbout("Опытный разработчик")
                .withCity("Moscow")
                .withExperience((short) 5)
                .withAvatar(avatar)
                .build();
    }

    // Возвращает билдера сущности пользователя для гибкой настройки полей.
    public static UserTestBuilder userBuilder() {
        return new UserTestBuilder();
    }

    /**
     * Билдер пользователя, позволяющий настраивать отдельные поля сущности.
     */
    public static final class UserTestBuilder {

        private String username;
        private String email;
        private String phone;
        private String password;
        private Boolean active;
        private String about;
        private String countryTitle;
        private String city;
        private Short experience;
        private UserProfileAvatar avatar;

        UserTestBuilder() {
        }

        // Устанавливает имя пользователя.
        public UserTestBuilder withUsername(String value) {
            this.username = value;
            return this;
        }

        // Устанавливает электронную почту пользователя.
        public UserTestBuilder withEmail(String value) {
            this.email = value;
            return this;
        }

        // Устанавливает телефон пользователя.
        public UserTestBuilder withPhone(String value) {
            this.phone = value;
            return this;
        }

        // Устанавливает пароль пользователя.
        public UserTestBuilder withPassword(String value) {
            this.password = value;
            return this;
        }

        // Устанавливает флаг активности пользователя.
        public UserTestBuilder withActive(Boolean value) {
            this.active = value;
            return this;
        }

        // Устанавливает описание пользователя.
        public UserTestBuilder withAbout(String value) {
            this.about = value;
            return this;
        }

        // Устанавливает название страны пользователя.
        public UserTestBuilder withCountryTitle(String value) {
            this.countryTitle = value;
            return this;
        }

        // Устанавливает город пользователя.
        public UserTestBuilder withCity(String value) {
            this.city = value;
            return this;
        }

        // Устанавливает опыт пользователя.
        public UserTestBuilder withExperience(short value) {
            this.experience = value;
            return this;
        }

        // Устанавливает аватар пользователя.
        public UserTestBuilder withAvatar(UserProfileAvatar value) {
            this.avatar = value;
            return this;
        }

        // Строит сущность пользователя на основе заданных полей.
        public User build() {
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
                    .userProfileAvatar(avatar)
                    .build();
        }
    }
}
