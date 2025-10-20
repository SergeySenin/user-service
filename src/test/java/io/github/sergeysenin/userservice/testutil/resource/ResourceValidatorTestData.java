package io.github.sergeysenin.userservice.testutil.resource;

import io.github.sergeysenin.userservice.config.avatar.AvatarProperties;
import io.github.sergeysenin.userservice.testutil.multipart.MultipartFileTestUtils.MultipartFileTestBuilder;

import java.util.List;

import static io.github.sergeysenin.userservice.testutil.multipart.MultipartFileTestUtils.multipartFile;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Утильные методы для подготовки данных и проверок в тестах {@code ResourceValidator}.
 */
public final class ResourceValidatorTestData {

    private ResourceValidatorTestData() {
    }

    // Создаёт билдера multipart-файла с типичными значениями для аватаров.
    public static MultipartFileTestBuilder avatarMultipartFileBuilder(
            String parameterName,
            String originalFilename,
            String contentType,
            byte[] content
    ) {
        return multipartFile()
                .withName(parameterName)
                .withOriginalFilename(originalFilename)
                .withContentType(contentType)
                .withContent(content)
                .withSize(content == null ? 0L : content.length);
    }

    // Настраивает mock AvatarProperties с заданным списком разрешённых MIME-типов.
    public static void mockAllowedMimeTypes(AvatarProperties avatarProperties, List<String> allowedMimeTypes) {
        when(avatarProperties.allowedMimeTypes()).thenReturn(allowedMimeTypes);
    }

    // Проверяет, что список разрешённых MIME-типов был запрошен ровно один раз.
    public static void verifyAllowedMimeTypesRequestedOnce(AvatarProperties avatarProperties) {
        verify(avatarProperties).allowedMimeTypes();
        verifyNoMoreInteractions(avatarProperties);
    }

    // Проверяет, что с AvatarProperties не было взаимодействий.
    public static void verifyNoInteractionsWithAvatarProperties(AvatarProperties avatarProperties) {
        verifyNoInteractions(avatarProperties);
    }
}
