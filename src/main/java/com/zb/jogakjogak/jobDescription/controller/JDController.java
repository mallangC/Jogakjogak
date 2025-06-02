package com.zb.jogakjogak.jobDescription.controller;

import com.zb.jogakjogak.global.HttpApiResponse;
import com.zb.jogakjogak.jobDescription.domain.requestDto.JDRequestDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.JDResponseDto;
import com.zb.jogakjogak.jobDescription.service.JDService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class JDController {

    private final JDService jdService;

    @PostMapping("/jd")
    public ResponseEntity<HttpApiResponse<JDResponseDto>> requestSend(@RequestBody JDRequestDto jdRequestDto) {
        return ResponseEntity.ok(new HttpApiResponse<>(jdService.analyze(jdRequestDto), "JD 분석하기 완료", HttpStatus.OK));
    }
}
