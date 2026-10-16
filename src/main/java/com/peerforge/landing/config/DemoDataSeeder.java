package com.peerforge.landing.config;

import com.peerforge.mentor.entity.ApprovalStatus;
import com.peerforge.mentor.entity.MentorAvailability;
import com.peerforge.mentor.entity.MentorProfile;
import com.peerforge.mentor.repository.MentorAvailabilityRepository;
import com.peerforge.mentor.repository.MentorProfileRepository;
import com.peerforge.review.entity.MentorReview;
import com.peerforge.review.entity.ReviewStatus;
import com.peerforge.review.repository.MentorReviewRepository;
import com.peerforge.role.entity.Role;
import com.peerforge.role.entity.RoleName;
import com.peerforge.role.repository.RoleRepository;
import com.peerforge.session.entity.Session;
import com.peerforge.session.entity.SessionStatus;
import com.peerforge.session.repository.SessionRepository;
import com.peerforge.skill.entity.Skill;
import com.peerforge.skill.repository.SkillRepository;
import com.peerforge.user.entity.AccountStatus;
import com.peerforge.user.entity.User;
import com.peerforge.user.entity.UserProfile;
import com.peerforge.user.repository.UserProfileRepository;
import com.peerforge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@Profile("demo & !prod")
@RequiredArgsConstructor
public class DemoDataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final MentorAvailabilityRepository availabilityRepository;
    private final RoleRepository roleRepository;
    private final SkillRepository skillRepository;
    private final SessionRepository sessionRepository;
    private final MentorReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${peerforge.demo.password:}")
    private String demoPassword;

    @Transactional
    public void run(ApplicationArguments args) {
        if (demoPassword == null || demoPassword.isBlank()) {
            throw new IllegalStateException(
                    "Set PEERFORGE_DEMO_PASSWORD before starting with the demo profile"
            );
        }

        if (userRepository.existsByEmail("asha.nair.demo@peerforge.local")) {
            return;
        }

        Role clientRole = getRole(RoleName.CLIENT);
        Role mentorRole = getRole(RoleName.MENTOR);
        User demoClient = createUser("Demo", "Learner", "learner.demo@peerforge.local", clientRole);

        List<DemoMentor> mentors = List.of(
                new DemoMentor(
                        "Asha", "Nair", "asha.nair.demo@peerforge.local",
                        "Senior Backend Engineer",
                        "I help developers design reliable REST APIs, work confidently with PostgreSQL, and prepare for backend engineering interviews.",
                        "Java", "Spring Boot", "PostgreSQL", "Microservices",
                        "MONDAY", 5, "A clear, practical session. I left with a useful API design checklist."
                ),
                new DemoMentor(
                        "Rohan", "Mehta", "rohan.mehta.demo@peerforge.local",
                        "Frontend Engineer",
                        "I work with learners on React fundamentals, component architecture, accessibility, and building maintainable TypeScript applications.",
                        "React", "TypeScript", "Node.js", "JavaScript",
                        "TUESDAY", 4, "The walkthrough made state management much easier to understand."
                ),
                new DemoMentor(
                        "Neha", "Kapoor", "neha.kapoor.demo@peerforge.local",
                        "Cloud Solutions Architect",
                        "I guide engineers through cloud fundamentals, system design tradeoffs, Docker, and deployment planning for production services.",
                        "AWS", "Docker", "System Design", "Python",
                        "WEDNESDAY", 5, "We broke a large system design problem into manageable decisions."
                )
        );

        int index = 0;
        for (DemoMentor sample : mentors) {
            Role mentorRoleCopy = mentorRole;
            User mentorUser = createUser(
                    sample.firstName(), sample.lastName(), sample.email(), clientRole, mentorRoleCopy
            );
            UserProfile profile = UserProfile.builder()
                    .user(mentorUser)
                    .headline(sample.headline())
                    .bio(sample.about())
                    .yearsOfExperience(BigDecimal.valueOf(5 + index, 0))
                    .build();
            userProfileRepository.save(profile);

            MentorProfile mentor = mentorProfileRepository.save(MentorProfile.builder()
                    .user(mentorUser)
                    .about(sample.about())
                    .hourlyRate(new BigDecimal("1500.00").add(BigDecimal.valueOf(index * 250L)))
                    .approvalStatus(ApprovalStatus.APPROVED)
                    .sessionsCompleted(1)
                    .averageRating(BigDecimal.valueOf(sample.rating()).setScale(1))
                    .reviewCount(1)
                    .build());

            for (String skillName : sample.skills()) {
                Skill skill = skillRepository.findByNameIgnoreCase(skillName)
                        .orElseGet(() -> skillRepository.save(Skill.builder().name(skillName).build()));
                mentorUser.getSkills().add(skill);
            }
            userRepository.save(mentorUser);

            DayOfWeek day = DayOfWeek.valueOf(sample.availabilityDay());
            availabilityRepository.save(MentorAvailability.builder()
                    .mentorProfile(mentor)
                    .dayOfWeek(day)
                    .startTime(LocalTime.of(17, 0))
                    .endTime(LocalTime.of(20, 0))
                    .build());

            LocalDateTime start = LocalDateTime.now().minusDays(7L + index).withHour(11).withMinute(0).withSecond(0).withNano(0);
            Session completedSession = sessionRepository.save(Session.builder()
                    .mentor(mentor)
                    .client(demoClient)
                    .startDateTime(start)
                    .endDateTime(start.plusHours(1))
                    .status(SessionStatus.COMPLETED)
                    .build());
            reviewRepository.save(MentorReview.builder()
                    .session(completedSession)
                    .mentor(mentor)
                    .client(demoClient)
                    .rating(sample.rating())
                    .comment(sample.review())
                    .status(ReviewStatus.APPROVED)
                    .build());
            index++;
        }
    }

    private User createUser(String firstName, String lastName, String email, Role... roles) {
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(demoPassword))
                .accountStatus(AccountStatus.ACTIVE)
                .build();
        user.getRoles().addAll(List.of(roles));
        return userRepository.save(user);
    }

    private Role getRole(RoleName roleName) {
        return roleRepository.findByName(roleName.name())
                .orElseThrow(() -> new IllegalStateException("Missing role " + roleName));
    }

    private record DemoMentor(
            String firstName,
            String lastName,
            String email,
            String headline,
            String about,
            String skill1,
            String skill2,
            String skill3,
            String skill4,
            String availabilityDay,
            int rating,
            String review
    ) {
        List<String> skills() { return List.of(skill1, skill2, skill3, skill4); }
    }
}
