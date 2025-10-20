package io.github.sergeysenin.userservice.testutil.net;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Утильные методы для работы с URL в тестах.
 */
public final class UrlTestUtils {

    private UrlTestUtils() {
    }

    // Создаёт URL для тестовых данных и упрощает обработку исключений.
    public static URL createUrl(String value) {
        try {
            return new URL(value);
        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException("Неверный формат URL для тестовых данных", exception);
        }
    }
}
