package com.peerforge.skill.mapper;

import com.peerforge.skill.dto.request.CreateSkillRequest;
import com.peerforge.skill.dto.response.SkillResponse;
import com.peerforge.skill.entity.Skill;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SkillMapper {

    Skill toEntity(CreateSkillRequest request);

    SkillResponse toResponse(Skill skill);
}