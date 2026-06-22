package com.peerforge.mentor.repository;

import com.peerforge.mentor.entity.MentorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MentorAvailabilityRepository
        extends JpaRepository<MentorAvailability, Long> {
    List<MentorAvailability> findByMentorProfileId(Long mentorId);
}