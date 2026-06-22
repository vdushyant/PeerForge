package com.peerforge.skill.service;

import com.peerforge.skill.dto.request.CreateSkillRequest;
import com.peerforge.skill.dto.response.SkillResponse;

import java.util.List;

public interface SkillService {

    SkillResponse createSkill(
            CreateSkillRequest request
    );

    List<SkillResponse> getAllSkills();
}