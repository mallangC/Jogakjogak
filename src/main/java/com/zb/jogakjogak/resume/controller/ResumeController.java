package com.zb.jogakjogak.resume.controller;

import com.zb.jogakjogak.global.HttpApiResponse;
import com.zb.jogakjogak.resume.domain.requestDto.ResumeAddRequestDto;
import com.zb.jogakjogak.resume.domain.requestDto.ResumeRequestDto;
import com.zb.jogakjogak.resume.domain.responseDto.ResumeAddResponseDto;
import com.zb.jogakjogak.resume.domain.responseDto.ResumeGetResponseDto;
import com.zb.jogakjogak.resume.domain.responseDto.ResumeResponseDto;
import com.zb.jogakjogak.resume.service.ResumeService;
import com.zb.jogakjogak.security.dto.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "이력서 관리 API", description = "이력서 등록, 수정, 조회, 삭제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping
public class ResumeController {

    private final ResumeService resumeService;

    /**
     * 이력서 등록을 위한 컨틀로러 메소드
     *
     * @param requestDto 이력서 이름, 이력서 내용
     * @return data(이력서 id, 이력서 이름, 이력서 내용), 성공 여부 메세지, 상태코드
     */
    @Operation(summary = "이력서 등록", description = "분석할 사용자의 이력서를 등록합니다")
    @PostMapping("/resume")
    public ResponseEntity<HttpApiResponse<ResumeResponseDto>> register(
            @Valid @RequestBody ResumeRequestDto requestDto,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        resumeService.register(requestDto, customOAuth2User.getMember()),
                        "이력서 등록 완료",
                        HttpStatus.CREATED
                )
        );
    }

    /**
     * 이력서 수정을 위한 컨트롤러 메서드
     *
     * @param resumeId   수정하려는 이력서의 id
     * @param requestDto 수정할 이력서 이름, 수정할 이력서 내용
     * @return data(수정한 이력서 id, 수정된 이력서 이름, 수정된 이력서 내용), 성공 여부 메세지, 상태코드
     */
    @Operation(summary = "이력서 수정", description = "사용자가 등록한 이력서를 수정합니다")
    @PatchMapping("/resume/{resume_id}")
    public ResponseEntity<HttpApiResponse<ResumeResponseDto>> modify(
            @PathVariable("resume_id") Long resumeId,
            @Valid @RequestBody ResumeRequestDto requestDto,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        return ResponseEntity.ok()
                .body(
                        new HttpApiResponse<>(
                                resumeService.modify(resumeId, requestDto, customOAuth2User.getMember()),
                                "이력서 수정 완료",
                                HttpStatus.OK
                        )
                );
    }

    /**
     * 사용자가 작성한 이력서를 조회하는 컨트롤러 메서드
     *
     * @param resumeId 찾으려는 이력서의 id
     * @return 찾으려는 이력서의 data, 성공 여부 메세지, 상태코드
     */
    @Operation(summary = "이력서 조회", description = "사용자가 등록한 이력서를 조회합니다")
    @GetMapping("/resume/{resumeId}")
    public ResponseEntity<HttpApiResponse<ResumeResponseDto>> get(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        return ResponseEntity.ok()
                .body(
                        new HttpApiResponse<>(
                                resumeService.get(resumeId, customOAuth2User.getMember()),
                                "이력서 조회 성공",
                                HttpStatus.OK
                        )
                );
    }

    @Operation(summary = "이력서 삭제", description = "사용자가 등록한 이력서를 삭제합니다")
    @DeleteMapping("/resume/{resumeId}")
    public ResponseEntity<HttpApiResponse<String>> delete(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        resumeService.delete(resumeId, customOAuth2User.getMember());
        return ResponseEntity.ok()
                .body(
                        new HttpApiResponse<>(
                                "",
                                "이력서 삭제 성공",
                                HttpStatus.NO_CONTENT
                        )
                );
    }

    /**
     * (v2)이력서 등록을 위한 컨트롤러 메소드
     *
     * @param requestDto 이력서 내용, 신입 유무, 경력 리스트, 학력 리스트, 스킬 리스트
     * @return data(이력서 id, 이력서 내용, 신입 유무, 경력 리스트, 학력 리스트, 스킬 리스트, 생성일시, 수정일시), 성공 여부 메세지, 상태코드
     */
    @Operation(summary = "(v2) 이력서 등록", description = "분석할 사용자의 이력서를 등록합니다")
    @PostMapping("/v2/resume")
    public ResponseEntity<HttpApiResponse<ResumeGetResponseDto>> registerV2(
            @Valid @RequestBody ResumeAddRequestDto requestDto,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        resumeService.registerV2(requestDto, customOAuth2User.getMember()),
                        "이력서 등록 완료",
                        HttpStatus.CREATED
                )
        );
    }

    /**
     * 사용자가 작성한 이력서를 조회하는 컨트롤러 메서드
     *
     * @return 찾으려는 이력서의 data, 성공 여부 메세지, 상태코드
     */
    @Operation(summary = "(v2)이력서 조회", description = "사용자가 등록한 이력서를 조회합니다")
    @GetMapping("/v2/resume")
    public ResponseEntity<HttpApiResponse<ResumeGetResponseDto>> getResumeV2(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        return ResponseEntity.ok()
                .body(
                        new HttpApiResponse<>(
                                resumeService.getResumeV2(customOAuth2User.getMember()),
                                "이력서 조회 성공",
                                HttpStatus.OK
                        )
                );
    }

    /**
     * 이력서 수정을 위한 컨트롤러 메서드
     *
     * @param requestDto 수정할 이력서 이름, 수정할 이력서 내용
     * @return data(수정한 이력서 id, 수정된 이력서 이름, 수정된 이력서 내용), 성공 여부 메세지, 상태코드
     */
    @Operation(summary = "이력서 수정", description = "사용자가 등록한 이력서를 수정합니다")
    @PatchMapping("/v2/resume")
    public ResponseEntity<HttpApiResponse<ResumeGetResponseDto>> modifyV2(
            @Valid @RequestBody ResumeAddRequestDto requestDto,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        return ResponseEntity.ok()
                .body(
                        new HttpApiResponse<>(
                                resumeService.modifyV2(requestDto, customOAuth2User.getMember()),
                                "이력서 수정 완료",
                                HttpStatus.OK
                        )
                );
    }
}
