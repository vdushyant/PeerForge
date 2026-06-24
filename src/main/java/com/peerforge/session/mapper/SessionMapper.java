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

    @Mapping(
            target = "mentorName",
            expression =
                    "java(session.getMentor().getUser().getFirstName() + " +
                            "\" \" + " +
                            "session.getMentor().getUser().getLastName())"
    )

    @Mapping(
            target = "clientName",
            expression =
                    "java(session.getClient().getFirstName() + " +
                            "\" \" + " +
                            "session.getClient().getLastName())"
    )

    @Mapping(
            target = "mentorEmail",
            source = "mentor.user.email"
    )

    @Mapping(
            target = "clientEmail",
            source = "client.email"
    )

    SessionResponse toResponse(
            Session session
    );

}