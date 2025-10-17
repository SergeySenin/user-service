package io.github.sergeysenin.userservice.config.avatar;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "user.avatar")
public record AvatarProperties(

        String storagePath,

        @Valid
        AvatarSizesProperties sizes,

        List<String> allowedMimeTypes
) {

    private static final List<String> DEFAULT_ALLOWED_MIME_TYPES = List.of("image/jpeg", "image/png", "image/webp");

    public AvatarProperties(

            @DefaultValue("avatars")
            String storagePath,

            AvatarSizesProperties sizes,

            @DefaultValue({"image/jpeg", "image/png", "image/webp"})
            List<String> allowedMimeTypes
    ) {
        this.storagePath = storagePath;
        this.sizes = sizes == null ? new AvatarSizesProperties(null, null) : sizes;
        this.allowedMimeTypes = allowedMimeTypes == null ? DEFAULT_ALLOWED_MIME_TYPES : allowedMimeTypes;
    }

    public record AvatarSizesProperties(

            AvatarSizeProperties thumbnail,
            AvatarSizeProperties profile
    ) {

        public AvatarSizesProperties(

                AvatarSizeProperties thumbnail,
                AvatarSizeProperties profile
        ) {
            this.thumbnail = thumbnail == null ? new AvatarSizeProperties(170) : thumbnail;
            this.profile = profile == null ? new AvatarSizeProperties(1080) : profile;
        }
    }

    public record AvatarSizeProperties(

            @Positive
            int maxSide
    ) {

        public AvatarSizeProperties(int maxSide) {
            this.maxSide = maxSide;
        }
    }
}
