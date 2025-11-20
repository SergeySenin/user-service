package io.github.sergeysenin.userservice.entity.user;

import io.github.sergeysenin.userservice.entity.user.country.Country;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PROTECTED)
public class User {

    @Id
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", length = 64, nullable = false, unique = true)
    private String username;

    @Column(name = "email", length = 256, nullable = false, unique = true)
    private String email;

    @Column(name = "phone", length = 16, nullable = false, unique = true)
    private String phone;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "about_me", length = 2048)
    private String aboutMe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Column(name = "city", length = 64)
    private String city;

    @Column(name = "experience")
    private Short experience;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "originalPath",
                    column = @Column(name = "avatar_original_path", length = 256)
            ),
            @AttributeOverride(
                    name = "thumbnailPath",
                    column = @Column(name = "avatar_thumbnail_path", length = 256)
            ),
            @AttributeOverride(
                    name = "profilePath",
                    column = @Column(name = "avatar_profile_path", length = 256)
            )
    })
    private UserProfileAvatar userProfileAvatar;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Builder(builderClassName = "UserBuilder", builderMethodName = "builder", access = AccessLevel.PUBLIC)
    static User createUser(
            String username,
            String email,
            String phone,
            Boolean active,
            String aboutMe,
            Country country,
            String city,
            Short experience,
            UserProfileAvatar userProfileAvatar
    ) {
        User user = new User();
        user.username = username;
        user.email = email;
        user.phone = phone;
        if (active != null) {
            user.active = active;
        }
        user.aboutMe = aboutMe;
        user.country = country;
        user.city = city;
        user.experience = experience;
        user.userProfileAvatar = userProfileAvatar;
        return user;
    }

    public void updateProfile(
            String username,
            String email,
            String phone,
            Boolean active,
            String aboutMe,
            Country country,
            String city,
            Short experience
    ) {
        if (username != null) {
            this.username = username;
        }
        if (email != null) {
            this.email = email;
        }
        if (phone != null) {
            this.phone = phone;
        }
        if (active != null) {
            this.active = active;
        }
        if (aboutMe != null) {
            this.aboutMe = aboutMe;
        }
        if (country != null) {
            this.country = country;
        }
        if (city != null) {
            this.city = city;
        }
        if (experience != null) {
            this.experience = experience;
        }
    }

    public boolean hasAvatar() {
        return userProfileAvatar != null;
    }

    public void updateAvatar(UserProfileAvatar avatar) {
        this.userProfileAvatar = avatar;
    }
}
