package io.github.sergeysenin.userservice.config.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Component
public class UserSecurity {

    private static final Logger log = LoggerFactory.getLogger(UserSecurity.class);
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final String userIdClaim;

    public UserSecurity(@Value("${app.security.jwt.user-id-claim:user_id}") String userIdClaim) {
        if (userIdClaim == null || userIdClaim.isBlank()) {
            throw new IllegalArgumentException("Название claim с идентификатором пользователя не может быть пустым");
        }
        this.userIdClaim = userIdClaim;
    }

    public boolean canAccessUserResource(Long userId, Authentication authentication) {
        if (userId == null) {
            log.debug("Отказано в доступе: userId не задан");
            return false;
        }

        ContextResolution resolution = resolveContext(authentication);
        Optional<UserContext> userContext = resolution.context();
        if (userContext.isEmpty()) {
            log.debug("Отказано в доступе: {}", resolution.denialReason());
            return false;
        }

        UserContext context = userContext.get();
        if (context.admin()) {
            return true;
        }
        if (context.isOwner(userId)) {
            return true;
        }

        log.debug(
                "Отказано в доступе: идентификатор из токена ({}) не совпадает с userId из запроса ({})",
                context.userId(),
                userId
        );
        return false;
    }

    public boolean isAdmin(Authentication authentication) {
        return resolveContext(authentication)
                .context()
                .map(UserContext::admin)
                .orElse(false);
    }

    public boolean isOwner(Long userId, Authentication authentication) {
        if (userId == null) {
            return false;
        }
        return resolveContext(authentication)
                .context()
                .map(context -> context.isOwner(userId))
                .orElse(false);
    }

    private ContextResolution resolveContext(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ContextResolution.denied("отсутствует аутентификация");
        }
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken)) {
            return ContextResolution.denied(
                    String.format("тип аутентификации %s не поддерживается", authentication.getClass().getName())
            );
        }

        Jwt token = jwtAuthenticationToken.getToken();
        Optional<Long> userId = extractUserId(token);
        if (userId.isEmpty()) {
            return ContextResolution.denied("не удалось извлечь идентификатор пользователя из токена");
        }

        boolean admin = hasAdminRole(authentication.getAuthorities());
        return ContextResolution.allowed(new UserContext(userId.get(), admin));
    }

    private boolean hasAdminRole(Collection<? extends GrantedAuthority> authorities) {
        if (authorities == null) {
            return false;
        }
        return authorities.stream().anyMatch(authority -> ROLE_ADMIN.equals(authority.getAuthority()));
    }

    private Optional<Long> extractUserId(Jwt token) {
        Object claimValue = token.getClaim(userIdClaim);
        if (claimValue == null && !"sub".equals(userIdClaim)) {
            claimValue = token.getClaim("sub");
        }
        if (claimValue == null) {
            return Optional.empty();
        }

        if (claimValue instanceof Number number) {
            return Optional.of(number.longValue());
        }
        if (claimValue instanceof String stringValue) {
            try {
                return Optional.of(Long.parseLong(stringValue));
            } catch (NumberFormatException exception) {
                log.debug(
                        "Не удалось преобразовать значение claim '{}'={} к Long",
                        userIdClaim,
                        stringValue,
                        exception
                );
                return Optional.empty();
            }
        }

        log.debug("Claim '{}' имеет неподдерживаемый тип: {}", userIdClaim, claimValue.getClass().getName());
        return Optional.empty();
    }

    private record UserContext(Long userId, boolean admin) {

        boolean isOwner(Long requestedUserId) {
            return Objects.equals(userId, requestedUserId);
        }
    }

    private record ContextResolution(Optional<UserContext> context, String denialReason) {

        static ContextResolution denied(String reason) {
            return new ContextResolution(Optional.empty(), reason);
        }

        static ContextResolution allowed(UserContext context) {
            return new ContextResolution(Optional.of(context), null);
        }
    }
}
