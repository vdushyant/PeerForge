package com.peerforge.session.mapper;

import com.peerforge.session.dto.response.SessionResponse;
import com.peerforge.session.entity.Session;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SessionMapper {
    @Mapping(
            target = "mentorId",
            source = "mentor.id"
    )

    @Mapping(
            target = "clientId",
            source = "client.id"
    )

    SessionResponse toResponse(
            Session session
    );

}