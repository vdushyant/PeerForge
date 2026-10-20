package com.peerforge.payment.gateway.dto;

import java.time.Instant;

public record GatewayQrCode(String qrCodeId, String imageUrl, Instant expiresAt) {
}
