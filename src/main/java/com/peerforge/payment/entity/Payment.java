package com.peerforge.payment.entity;

import com.peerforge.common.entity.BaseEntity;
import com.peerforge.session.entity.Session;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "session_id",
            nullable = false,
            unique = true
    )
    private Session session;

    @Column(
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal amount;

    @Column(name = "mentor_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal mentorAmount;

    @Column(name = "client_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal clientFee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "provider_order_id")
    private String providerOrderId;

    @Column(name = "provider_payment_id")
    private String providerPaymentId;

    @Column(name = "provider_signature")
    private String providerSignature;

    @Column(name = "provider_qr_code_id", unique = true)
    private String providerQrCodeId;

    @Column(name = "provider_qr_code_image_url", length = 1000)
    private String providerQrCodeImageUrl;

    @Column(name = "provider_qr_code_expires_at")
    private Instant providerQrCodeExpiresAt;

}
