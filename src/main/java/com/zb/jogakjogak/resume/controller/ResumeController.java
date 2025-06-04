package com.zb.jogakjogak.resume.controller;

import com.zb.jogakjogak.global.HttpApiResponse;
import com.zb.jogakjogak.resume.domain.ResumeRequestDto;
import com.zb.jogakjogak.resume.service.ResumeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resume")
public class ResumeController {

    private final ResumeService resumeService;

    /**
     * 이력서 등록을 위한 컨틀로러 메소드
     * @param requestDto 이력서 이름, 이력서 내용
     * @return data(이력서 id, 이력서 이름, 이력서 내용), 성공 여부 메세지, 상태코드
     */
    @PostMapping
    public ResponseEntity register(@Valid @RequestBody ResumeRequestDto requestDto) {
        return ResponseEntity.ok()
                .body(
                        new HttpApiResponse(
                                resumeService.register(requestDto),
                                "이력서 등록 완료",
                                HttpStatus.CREATED)
                );
    }

    @PatchMapping("/{resume_id")
    public ResponseEntity modify(@PathVariable Long resume_id, @Valid @RequestBody ResumeRequestDto requestDto) {
        return ResponseEntity.ok()
                .body(
                        new HttpApiResponse(
                                resumeService.modify(resume_id, requestDto),
                                "이력서 수정 완료",
                                HttpStatus.OK
                        )
                );
    }
}
