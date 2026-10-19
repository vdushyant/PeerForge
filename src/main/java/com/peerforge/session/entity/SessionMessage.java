package com.peerforge.session.entity;

import com.peerforge.common.entity.BaseEntity;
import com.peerforge.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "session_messages", indexes = {
        @Index(name = "idx_session_messages_session_created", columnList = "session_id, created_at, id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class SessionMessage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, length = 2000)
    private String message;
}
