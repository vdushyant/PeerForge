package com.peerforge.mentor.service.impl;

import com.peerforge.common.exception.DuplicateResourceException;
import com.peerforge.common.exception.MentorUnavailableException;
import com.peerforge.common.exception.ResourceNotFoundException;
import com.peerforge.mentor.dto.request.CreateAvailabilityRequest;
import com.peerforge.mentor.dto.request.MentorApplicationRequest;
import com.peerforge.mentor.dto.request.UpdateMentorProfileRequest;
import com.peerforge.mentor.dto.response.AvailabilityResponse;
import com.peerforge.mentor.dto.response.MentorCardResponse;
import com.peerforge.mentor.dto.response.MentorDetailResponse;
import com.peerforge.mentor.dto.response.MentorProfileResponse;
import com.peerforge.mentor.dto.response.MentorApplicationResponse;
import com.peerforge.mentor.entity.ApprovalStatus;
import com.peerforge.mentor.entity.MentorAvailability;
import com.peerforge.mentor.entity.MentorProfile;
import com.peerforge.notification.entity.NotificationType;
import com.peerforge.notification.service.NotificationService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    private final NotificationService notificationService;

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

        var existingProfile = mentorProfileRepository.findByUserId(user.getId());
        if (existingProfile.isPresent()) {
            MentorProfile existing = existingProfile.get();
            if (existing.getApprovalStatus() != ApprovalStatus.REJECTED) {
                throw new DuplicateResourceException("Mentor profile already exists");
            }
            existing.setAbout(request.about());
            existing.setHourlyRate(request.hourlyRate());
            existing.setApprovalStatus(ApprovalStatus.PENDING);
            existing.setRejectionReason(null);
            return mentorProfileMapper.toResponse(mentorProfileRepository.save(existing));
        }

        MentorProfile mentorProfile =
                mentorProfileMapper
                        .toEntity(request);

        mentorProfile.setUser(user);

        mentorProfile.setApprovalStatus(
                ApprovalStatus.PENDING
        );
        mentorProfile.setRejectionReason(null);

        mentorProfile.setSessionsCompleted(0);

        mentorProfile.setAverageRating(
                BigDecimal.ZERO
        );
        mentorProfile.setReviewCount(0);

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

                            mentor.getAverageRating(),

                            mentor.getReviewCount()
                    );

                })

                .toList();
    }

    @Override
    public MentorProfileResponse approveMentor(
            Long mentorId
    ) {

        MentorProfile mentorProfile = mentorProfileRepository.findById(mentorId).orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Mentor not found"
                                ));

        if (mentorProfile.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new DuplicateResourceException(
                    "Only pending mentor applications can be approved"
            );
        }

        Role mentorRole = roleRepository.findByName(RoleName.MENTOR.name())
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        User user = mentorProfile.getUser();
        user.getRoles().add(mentorRole);

        mentorProfile.setApprovalStatus(
                ApprovalStatus.APPROVED
        );
        mentorProfile.setRejectionReason(null);

        userRepository.save(user);

        MentorProfile updatedMentor =
                mentorProfileRepository
                        .save(mentorProfile);

        notificationService.notifyUser(
                user,
                NotificationType.MENTOR_APPLICATION_APPROVED,
                "Mentor application approved",
                "Your PeerForge mentor application is approved. You can now manage your profile and sessions.",
                "/mentor/dashboard"
        );

        return mentorProfileMapper
                .toResponse(updatedMentor);

    }

    @Override
    public MentorProfileResponse rejectMentor(Long mentorId, String reason) {
        MentorProfile mentor = mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));
        if (mentor.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new DuplicateResourceException("Only pending mentor applications can be rejected");
        }

        String normalizedReason = reason == null ? "" : reason.trim();
        if (normalizedReason.isBlank()) {
            throw new IllegalArgumentException("A rejection reason is required");
        }
        mentor.setApprovalStatus(ApprovalStatus.REJECTED);
        mentor.setRejectionReason(normalizedReason);
        MentorProfile saved = mentorProfileRepository.save(mentor);

        notificationService.notifyUser(
                mentor.getUser(),
                NotificationType.MENTOR_APPLICATION_REJECTED,
                "Mentor application needs changes",
                "Your application was not approved yet: " + normalizedReason
                        + " You can update your application and reapply.",
                "/mentor/become"
        );
        return mentorProfileMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MentorApplicationResponse> getMentorApplications(
            ApprovalStatus status,
            Pageable pageable
    ) {
        return mentorProfileRepository.findByApprovalStatusOrderByCreatedAtDesc(status, pageable)
                .map(mentor -> new MentorApplicationResponse(
                        mentor.getId(),
                        mentor.getUser().getFirstName(),
                        mentor.getUser().getLastName(),
                        mentor.getUser().getEmail(),
                        mentor.getAbout(),
                        mentor.getHourlyRate(),
                        mentor.getApprovalStatus(),
                        mentor.getRejectionReason(),
                        mentor.getCreatedAt()
                ));
    }

    @Override
    public MentorProfileResponse getMyMentorProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        MentorProfile mentorProfile = mentorProfileRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Mentor profile not found"
                        )
                );

        return mentorProfileMapper.toResponse(mentorProfile);
    }

    @Override
    public MentorProfileResponse updateMyMentorProfile(
            UpdateMentorProfileRequest request,
            String email
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        MentorProfile mentorProfile = mentorProfileRepository.findByUserId(user.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Mentor profile not found"));

        mentorProfile.setAbout(request.getAbout());
        mentorProfile.setHourlyRate(request.getHourlyRate());

        MentorProfile saved = mentorProfileRepository.save(mentorProfile);
        return mentorProfileMapper.toResponse(saved);
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


        MentorProfile mentor = mentorProfileRepository.findByUserId(user.getId()).orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Mentor not found"
                                ));

        if (mentor.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new MentorUnavailableException("Mentor profile is not approved");
        }

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

        MentorAvailability availability = mentorProfileMapper.toEntity(request);

        availability.setMentorProfile(mentor);
        MentorAvailability saved = mentorAvailabilityRepository.save(availability);
        return mentorProfileMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getMyAvailability(
            String email
    ) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        MentorProfile mentor = mentorProfileRepository.findByUserId(user.getId()).orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Mentor not found"
                                ));

        if (mentor.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new MentorUnavailableException("Mentor profile is not approved");
        }

        return mentorAvailabilityRepository
                .findByMentorProfileId(mentor.getId())
                .stream()
                .map(mentorProfileMapper::toResponse)
                .toList();
    }

    @Override
    public void deleteAvailability(
            Long availabilityId,
            String email
    ) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        MentorProfile mentor = mentorProfileRepository
                        .findByUserId(user.getId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Mentor not found"
                                ));

        if (mentor.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new MentorUnavailableException("Mentor profile is not approved");
        }

        MentorAvailability availability =
                mentorAvailabilityRepository
                        .findById(availabilityId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Availability not found"
                                ));

        if (!availability.getMentorProfile().getId()
                .equals(mentor.getId())) {

            throw new ResourceNotFoundException(
                    "Availability not found"
            );
        }

        mentorAvailabilityRepository.delete(availability);
    }

    @Override
    @Transactional(readOnly = true)
    public MentorDetailResponse getMentorById(Long mentorId) {

        MentorProfile mentor = mentorProfileRepository.findById(mentorId).orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Mentor not found"
                                ));

        if (mentor.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new ResourceNotFoundException(
                    "Mentor not found"
            );
        }

        return mentorProfileMapper.toDetailResponse(mentor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getMentorAvailability(Long mentorId) {

        MentorProfile mentor = mentorProfileRepository.findById(mentorId).orElseThrow(() ->
                                new ResourceNotFoundException("Mentor not found"));

        if (mentor.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new ResourceNotFoundException("Mentor not found");
        }

        return mentorAvailabilityRepository
                .findByMentorProfileId(mentorId)
                .stream()
                .map(mentorProfileMapper::toResponse)
                .toList();
    }

}
