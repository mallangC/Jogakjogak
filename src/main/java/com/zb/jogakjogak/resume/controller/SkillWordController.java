package com.zb.jogakjogak.resume.controller;

import com.zb.jogakjogak.global.HttpApiResponse;
import com.zb.jogakjogak.resume.service.SkillWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/resume/skill-word")
public class SkillWordController {

    private final SkillWordService skillWordService;

    @GetMapping
    public ResponseEntity<HttpApiResponse<List<String>>> autoComplete(
            @RequestParam("q") String query) {
        return ResponseEntity.ok()
                .body(
                        new HttpApiResponse<>(
                                skillWordService.getAutocompleteSuggestions(query),
                                "스킬 단어 검색 완료",
                                HttpStatus.OK)
                );
    }
}
