package com.peerforge.mentor.service.impl;

import com.peerforge.common.exception.DuplicateResourceException;
import com.peerforge.common.exception.ResourceNotFoundException;
import com.peerforge.mentor.dto.request.CreateAvailabilityRequest;
import com.peerforge.mentor.dto.request.MentorApplicationRequest;
import com.peerforge.mentor.dto.response.AvailabilityResponse;
import com.peerforge.mentor.dto.response.MentorCardResponse;
import com.peerforge.mentor.dto.response.MentorProfileResponse;
import com.peerforge.mentor.entity.ApprovalStatus;
import com.peerforge.mentor.entity.MentorAvailability;
import com.peerforge.mentor.entity.MentorProfile;
import com.peerforge.mentor.mapper.MentorProfileMapper;
import com.peerforge.mentor.repository.MentorAvailabilityRepository;
import com.peerforge.mentor.repository.MentorProfileRepository;
import com.peerforge.mentor.service.MentorService;
import com.peerforge.role.entity.Role;
import com.peerforge.role.entity.RoleName;
import com.peerforge.role.repository.RoleRepository;
import com.peerforge.skill.entity.Skill;
import com.peerforge.user.entity.User;
import com.peerforge.user.entity.UserProfile;
import com.peerforge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MentorServiceImpl
        implements MentorService {

    private final UserRepository userRepository;

    private final MentorProfileRepository
            mentorProfileRepository;

    private final MentorProfileMapper
            mentorProfileMapper;

    private final RoleRepository roleRepository;

    private final MentorAvailabilityRepository
            mentorAvailabilityRepository;

    @Override
    public MentorProfileResponse applyForMentorship(
            MentorApplicationRequest request,
            String email
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        if (mentorProfileRepository
                .findByUserId(user.getId())
                .isPresent()) {

            throw new DuplicateResourceException(
                    "Mentor profile already exists"
            );
        }

        MentorProfile mentorProfile =
                mentorProfileMapper
                        .toEntity(request);

        mentorProfile.setUser(user);

        mentorProfile.setApprovalStatus(
                ApprovalStatus.PENDING
        );

        mentorProfile.setSessionsCompleted(0);

        mentorProfile.setAverageRating(
                BigDecimal.ZERO
        );

        MentorProfile savedMentorProfile =
                mentorProfileRepository
                        .save(mentorProfile);

        return mentorProfileMapper.toResponse(
                savedMentorProfile
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<MentorCardResponse> getAllMentors() {

        return mentorProfileRepository
                .findAll()
                .stream()

                .filter(mentor ->
                        mentor.getApprovalStatus()
                                == ApprovalStatus.APPROVED
                )

                .map(mentor -> {

                    User user = mentor.getUser();

                    UserProfile profile =
                            user.getProfile();

                    List<String> skills =
                            user.getSkills()
                                    .stream()

                                    .map(Skill::getName)

                                    .sorted()

                                    .toList();

                    return new MentorCardResponse(

                            mentor.getId(),

                            user.getFirstName(),

                            user.getLastName(),

                            profile.getHeadline(),

                            profile.getYearsOfExperience(),

                            skills,

                            mentor.getHourlyRate(),

                            mentor.getAverageRating()
                    );

                })

                .toList();
    }

    @Override
    public MentorProfileResponse approveMentor(
            Long mentorId
    ) {

        MentorProfile mentorProfile =
                mentorProfileRepository
                        .findById(mentorId)

                        .orElseThrow(() ->

                                new ResourceNotFoundException(
                                        "Mentor not found"
                                ));

        if (mentorProfile.getApprovalStatus()
                == ApprovalStatus.APPROVED) {

            throw new DuplicateResourceException(
                    "Mentor already approved"
            );
        }

        Role mentorRole =
                roleRepository.findByName(
                                RoleName.MENTOR.name()
                        )

                        .orElseThrow(() ->

                                new ResourceNotFoundException(
                                        "Role not found"
                                ));

        User user = mentorProfile.getUser();

        user.getRoles().add(mentorRole);

        mentorProfile.setApprovalStatus(
                ApprovalStatus.APPROVED
        );

        userRepository.save(user);

        MentorProfile updatedMentor =
                mentorProfileRepository
                        .save(mentorProfile);

        return mentorProfileMapper
                .toResponse(updatedMentor);

    }

    @Override
    public AvailabilityResponse addAvailability(
            CreateAvailabilityRequest request,
            String email
    ) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));


        MentorProfile mentor =
                mentorProfileRepository
                        .findByUserId(
                                user.getId()
                        ).orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Mentor not found"
                                ));

        if (!request.startTime().isBefore(request.endTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }


        List<MentorAvailability> existingSlots = mentorAvailabilityRepository.findByMentorProfileId(mentor.getId());
        for (MentorAvailability slot : existingSlots) {
            if (slot.getDayOfWeek() != request.dayOfWeek()) {
                continue;
            }
            boolean noOverlap = slot.getEndTime().compareTo(request.startTime()) <= 0|| request.endTime().compareTo(slot.getStartTime()) <= 0;
            if (!noOverlap) {
                throw new DuplicateResourceException(
                        "Availability overlaps with existing slot"
                );
            }
        }

        MentorAvailability availability =
                mentorProfileMapper
                        .toEntity(
                                request
                        );


        availability.setMentorProfile(mentor);
        MentorAvailability saved = mentorAvailabilityRepository.save(availability);
        return mentorProfileMapper.toResponse(saved);
    }

}