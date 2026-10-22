package com.neuroforge.neuroforge.nexus.mapper;

import com.neuroforge.neuroforge.nexus.dto.request.SignupRequest;
import com.neuroforge.neuroforge.nexus.dto.response.SignupResponse;
import com.neuroforge.neuroforge.nexus.dto.response.UserSummaryResponse;
import com.neuroforge.neuroforge.nexus.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toEntity(SignupRequest request);

    SignupResponse toResponse(User user);

    List<SignupResponse> toAllResponse(List<User> user);

    UserSummaryResponse toSummary(User user);

    List<UserSummaryResponse> toSummaryList(List<User> users);
}