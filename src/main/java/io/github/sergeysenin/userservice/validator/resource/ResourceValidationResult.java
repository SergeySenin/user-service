package io.github.sergeysenin.userservice.validator.resource;

public record ResourceValidationResult(

        String canonicalExtension,
        String mimeType
) {
}
