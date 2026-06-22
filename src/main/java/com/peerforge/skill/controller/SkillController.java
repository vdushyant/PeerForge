package com.peerforge.skill.controller;

import com.peerforge.skill.dto.request.CreateSkillRequest;
import com.peerforge.skill.dto.response.SkillResponse;
import com.peerforge.skill.service.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SkillResponse createSkill(
            @Valid @RequestBody CreateSkillRequest request
    ) {

        return skillService.createSkill(
                request
        );
    }

    @GetMapping
    public List<SkillResponse> getAllSkills() {

        return skillService.getAllSkills();
    }
}