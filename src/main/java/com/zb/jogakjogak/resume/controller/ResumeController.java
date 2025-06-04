package com.zb.jogakjogak.resume.controller;

import com.zb.jogakjogak.global.HttpApiResponse;
import com.zb.jogakjogak.resume.domain.ResumeRequestDto;
import com.zb.jogakjogak.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resume")
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping
    public ResponseEntity register(@RequestBody ResumeRequestDto requestDto) {
        return ResponseEntity.ok()
                .body(
                        new HttpApiResponse(
                                resumeService.register(requestDto),
                                "이력서 등록 완료",
                                HttpStatus.CREATED)
                );
    }
}
