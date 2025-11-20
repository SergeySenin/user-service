package io.github.sergeysenin.userservice.mapper.user;

import io.github.sergeysenin.userservice.dto.user.CountrySummaryDto;
import io.github.sergeysenin.userservice.dto.user.UserResponse;
import io.github.sergeysenin.userservice.entity.user.User;
import io.github.sergeysenin.userservice.entity.user.country.Country;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "hasAvatar", expression = "java(user.hasAvatar())")
    UserResponse toResponse(User user);

    CountrySummaryDto toCountrySummary(Country country);
}
