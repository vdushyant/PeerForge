package com.peerforge.session.entity;

import com.peerforge.common.entity.BaseEntity;
import com.peerforge.mentor.entity.MentorProfile;
import com.peerforge.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Session extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "mentor_id",
            nullable = false)
    private MentorProfile mentor;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id",
            nullable = false)
    private User client;

    @Column(name = "start_datetime",
            nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_datetime",
            nullable = false)
    private LocalDateTime endDateTime;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;


    @Column(name = "meeting_link", length = 500)
    private String meetingLink;

}