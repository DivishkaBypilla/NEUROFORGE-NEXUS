package com.neuroforge.neuroforge.nexus.mapper;

import com.neuroforge.neuroforge.nexus.dto.request.SignupRequest;
import com.neuroforge.neuroforge.nexus.dto.response.SignupResponse;
import com.neuroforge.neuroforge.nexus.dto.response.UserSummaryResponse;
import com.neuroforge.neuroforge.nexus.entities.User;
import com.neuroforge.neuroforge.nexus.entities.enums.Role;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-17T18:34:45+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.4.1 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toEntity(SignupRequest request) {
        if ( request == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.name( request.name() );
        user.email( request.email() );
        user.password( request.password() );

        return user.build();
    }

    @Override
    public SignupResponse toResponse(User user) {
        if ( user == null ) {
            return null;
        }

        String id = null;
        String name = null;
        String email = null;
        Role role = null;

        id = user.getId();
        name = user.getName();
        email = user.getEmail();
        role = user.getRole();

        SignupResponse signupResponse = new SignupResponse( id, name, email, role );

        return signupResponse;
    }

    @Override
    public List<SignupResponse> toAllResponse(List<User> user) {
        if ( user == null ) {
            return null;
        }

        List<SignupResponse> list = new ArrayList<SignupResponse>( user.size() );
        for ( User user1 : user ) {
            list.add( toResponse( user1 ) );
        }

        return list;
    }

    @Override
    public UserSummaryResponse toSummary(User user) {
        if ( user == null ) {
            return null;
        }

        String id = null;
        String name = null;
        String email = null;
        Role role = null;

        id = user.getId();
        name = user.getName();
        email = user.getEmail();
        role = user.getRole();

        UserSummaryResponse userSummaryResponse = new UserSummaryResponse( id, name, email, role );

        return userSummaryResponse;
    }

    @Override
    public List<UserSummaryResponse> toSummaryList(List<User> users) {
        if ( users == null ) {
            return null;
        }

        List<UserSummaryResponse> list = new ArrayList<UserSummaryResponse>( users.size() );
        for ( User user : users ) {
            list.add( toSummary( user ) );
        }

        return list;
    }
}
