package com.peerforge.mentor.repository;

import com.peerforge.mentor.entity.MentorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MentorProfileRepository
        extends JpaRepository<MentorProfile, Long> {

    Optional<MentorProfile>
    findByUserId(Long userId);

}