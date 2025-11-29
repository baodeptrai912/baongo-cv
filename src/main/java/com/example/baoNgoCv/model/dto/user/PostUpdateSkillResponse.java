package com.example.baoNgoCv.model.dto.user;

import com.example.baoNgoCv.model.enums.Skill;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record PostUpdateSkillResponse(
        List<String> skills,
        String message,
        LocalDateTime updatedAt
) {}
