package com.zb.jogakjogak.resume.controller;

import com.zb.jogakjogak.global.HttpApiResponse;
import com.zb.jogakjogak.resume.domain.requestDto.ResumeRequestDto;
import com.zb.jogakjogak.resume.domain.responseDto.ResumeResponseDto;
import com.zb.jogakjogak.resume.service.ResumeService;
import com.zb.jogakjogak.security.dto.CustomOAuth2User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resume")
public class ResumeController {

    private final ResumeService resumeService;
    /**
     * 이력서 등록을 위한 컨틀로러 메소드
     *
     * @param requestDto 이력서 이름, 이력서 내용
     * @return data(이력서 id, 이력서 이름, 이력서 내용), 성공 여부 메세지, 상태코드
     */
    @PostMapping
    public ResponseEntity<HttpApiResponse<ResumeResponseDto>> register(
            @Valid @RequestBody ResumeRequestDto requestDto,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        String username = customOAuth2User.getName();
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        resumeService.register(requestDto, username),
                        "이력서 등록 완료",
                        HttpStatus.CREATED
                )
        );
    }
    /**
     * 이력서 수정을 위한 컨트롤러 메서드
     * @param resumeId 수정하려는 이력서의 id
     * @param requestDto 수정할 이력서 이름, 수정할 이력서 내용
     * @return data(수정한 이력서 id, 수정된 이력서 이름, 수정된 이력서 내용), 성공 여부 메세지, 상태코드
     */
    @PatchMapping("/{resume_id}")
    public ResponseEntity<HttpApiResponse<ResumeResponseDto>> modify(
            @PathVariable("resume_id") Long resumeId,
            @Valid @RequestBody ResumeRequestDto requestDto,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        String username = customOAuth2User.getName();
        return ResponseEntity.ok()
                .body(
                        new HttpApiResponse<>(
                                resumeService.modify(resumeId, requestDto, username),
                                "이력서 수정 완료",
                                HttpStatus.OK
                        )
                );
    }
    /**
     * 사용자가 작성한 이력서를 조회하는 컨트롤러 메서드
     * @param resumeId 찾으려는 이력서의 id
     * @return 찾으려는 이력서의 data, 성공 여부 메세지, 상태코드
     */
    @GetMapping("/{resumeId}")
    public ResponseEntity<HttpApiResponse<ResumeResponseDto>> get(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        String username = customOAuth2User.getName();
        return ResponseEntity.ok()
                .body(
                        new HttpApiResponse<>(
                                resumeService.get(resumeId, username),
                                "이력서 조회 성공",
                                HttpStatus.OK
                        )
                );
    }

    @DeleteMapping("/{resumeId}")
    public ResponseEntity<HttpApiResponse<String>> delete(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        String username = customOAuth2User.getName();
        resumeService.delete(resumeId, username);
        return ResponseEntity.ok()
                .body(
                        new HttpApiResponse<>(
                                "",
                                "이력서 삭제 성공",
                                HttpStatus.NO_CONTENT
                        )
                );
    }
}
