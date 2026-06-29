package com.peerforge.payment.gateway.dto;

public record GatewayOrder(

        String orderId,

        String currency

) {
}