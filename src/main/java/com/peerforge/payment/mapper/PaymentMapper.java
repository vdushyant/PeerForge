package com.peerforge.payment.mapper;

import com.peerforge.payment.dto.response.PaymentResponse;
import com.peerforge.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(
            target = "sessionId",
            source = "session.id"
    )
    PaymentResponse toResponse(Payment payment);

}