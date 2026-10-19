package com.peerforge.session.mapper;

import com.peerforge.session.dto.response.SessionResponse;
import com.peerforge.session.entity.Session;
import com.peerforge.payment.entity.PaymentStatus;
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

    @Mapping(target = "meetingLink", expression = "java(session.getPayment() != null && session.getPayment().getStatus() == PaymentStatus.SUCCESS ? session.getMeetingLink() : null)")
    @Mapping(target = "paymentStatus", expression = "java(session.getPayment() == null ? null : session.getPayment().getStatus())")

    SessionResponse toResponse(
            Session session
    );

}
