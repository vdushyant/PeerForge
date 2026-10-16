package com.peerforge.mentor.repository;

import com.peerforge.mentor.entity.MentorProfile;
import com.peerforge.mentor.entity.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MentorProfileRepository
        extends JpaRepository<MentorProfile, Long> {

    Optional<MentorProfile>
    findByUserId(Long userId);

    long countByApprovalStatus(ApprovalStatus approvalStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select mentor from MentorProfile mentor where mentor.id = :mentorId")
    Optional<MentorProfile> findByIdForUpdate(@Param("mentorId") Long mentorId);

}
