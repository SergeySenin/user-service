package io.github.sergeysenin.userservice.entity.user.country;

import io.github.sergeysenin.userservice.entity.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "countries")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PROTECTED)
public class Country {

    @Id
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", length = 64, nullable = false, unique = true)
    private String title;

    @OneToMany(mappedBy = "country")
    private List<User> residents = new ArrayList<>();

    @Builder(builderClassName = "CountryBuilder", builderMethodName = "builder", access = AccessLevel.PUBLIC)
    static Country createCountry(String title, List<User> residents) {
        Country country = new Country();
        country.title = title;
        if (residents != null) {
            country.residents = new ArrayList<>(residents);
        }
        return country;
    }
}
