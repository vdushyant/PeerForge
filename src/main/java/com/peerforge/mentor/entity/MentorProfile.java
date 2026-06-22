package com.peerforge.mentor.entity;

import com.peerforge.common.entity.BaseEntity;
import com.peerforge.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "mentor_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 2000)
    private String about;

    @Column(
            name = "hourly_rate",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal hourlyRate;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "approval_status",
            nullable = false
    )
    private ApprovalStatus approvalStatus;

    @Column(
            name = "sessions_completed",
            nullable = false
    )
    private Integer sessionsCompleted;

    @Column(
            name = "average_rating",
            precision = 2,
            scale = 1
    )
    private BigDecimal averageRating;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;
}