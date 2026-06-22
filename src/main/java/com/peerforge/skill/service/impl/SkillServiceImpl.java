package com.peerforge.skill.service.impl;

import com.peerforge.common.exception.DuplicateResourceException;
import com.peerforge.skill.dto.request.CreateSkillRequest;
import com.peerforge.skill.dto.response.SkillResponse;
import com.peerforge.skill.entity.Skill;
import com.peerforge.skill.mapper.SkillMapper;
import com.peerforge.skill.repository.SkillRepository;
import com.peerforge.skill.service.SkillService;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;

    @Override
    public SkillResponse createSkill(
            CreateSkillRequest request
    ) {

        String normalizedName =
                request.name().trim();

        if (skillRepository.existsByNameIgnoreCase(
                normalizedName
        )) {

            throw new DuplicateResourceException(
                    "Skill already exists"
            );
        }

        Skill skill = Skill.builder()
                .name(normalizedName)
                .build();

        Skill savedSkill =
                skillRepository.save(skill);

        return skillMapper.toResponse(
                savedSkill
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillResponse> getAllSkills() {

        return skillRepository.findAll()
                .stream()
                .map(skillMapper::toResponse)
                .toList();
    }
}