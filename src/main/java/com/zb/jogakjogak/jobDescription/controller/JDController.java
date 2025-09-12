package com.zb.jogakjogak.jobDescription.controller;

import com.zb.jogakjogak.global.HttpApiResponse;
import com.zb.jogakjogak.jobDescription.domain.requestDto.*;
import com.zb.jogakjogak.jobDescription.domain.responseDto.*;
import com.zb.jogakjogak.jobDescription.service.JDService;
import com.zb.jogakjogak.security.dto.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "분석 관리 API",
        description = "GEMINI API 기반 JD/이력서 분석 관련 API")
@RestController
@RequiredArgsConstructor
public class JDController {

    private final JDService jdService;

    @Operation(summary = "JD와 이력서를 분석 후 Todolist 생성",
            description = "등록된 이력서와 요청으로 받은 JD를 함께 분석 후 Todolist 형식으로 응답합니다")
    @PostMapping("/jds")
    public ResponseEntity<HttpApiResponse<JDResponseDto>> llmAnalyze(
            @Valid @RequestBody JDRequestDto jdRequestDto,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        jdService.llmAnalyze(jdRequestDto, customOAuth2User.getMember()),
                        "JD 분석하기 완료",
                        HttpStatus.CREATED
                )
        );
    }

    @Operation(summary = "특정 분석 내용 단건 조회", description = "jd_id를 통해 해당 분석 내용을 단건으로 조회합니다")
    @GetMapping("/jds/{jd_id}")
    public ResponseEntity<HttpApiResponse<JDResponseDto>> getJd(
            @PathVariable("jd_id") Long jdId,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        jdService.getJd(jdId, customOAuth2User.getMember()),
                        "나의 분석 내용 단일 조회 완료",
                        HttpStatus.OK
                )
        );
    }

    @Operation(summary = "특정 분석 알림 설정", description = "jd_id를 통해 특정 분석에 대한 이메일 알림에 대한 설정/미설정합니다")
    @PatchMapping("/jds/{jd_id}/alarm")
    public ResponseEntity<HttpApiResponse<JDAlarmResponseDto>> alarm(
            @PathVariable("jd_id") Long jdId,
            @RequestBody JDAlarmRequestDto dto,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        jdService.alarm(jdId, dto, customOAuth2User.getMember()),
                        "알람 설정 완료",
                        HttpStatus.OK
                )
        );
    }

    @Operation(summary = "특정 분석 삭제", description = "jd_id를 통해 특정 분석을 삭제합니다")
    @DeleteMapping("/jds/{jd_id}")
    public ResponseEntity<HttpApiResponse<String>> deleteJd(
            @PathVariable("jd_id") Long jdId,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        jdService.deleteJd(jdId, customOAuth2User.getMember());
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        "",
                        "나의 분석 내용 삭제 성공",
                        HttpStatus.NO_CONTENT
                )
        );
    }

    /**
     * 현재 인증된 사용자의 모든 JD (Job Description) 목록을 페이징하여 조회합니다.
     * 기본적으로 한 페이지에 11개의 항목이 표시되며, 생성일(createdAt)을 기준으로 최신순으로 정렬됩니다.
     * 클라이언트가 'page', 'size', 'sort' 파라미터를 통해 페이징 및 정렬 조건을 직접 지정할 수도 있습니다.
     *
     * @param pageable         페이징 및 정렬 정보를 담는 객체.
     *                         - size: 한 페이지당 항목 수 (기본값 11)
     *                         - sort: 정렬 기준 필드 (기본값 "createdAt")
     *                         - direction: 정렬 방향 (기본값 DESC, 즉 최신순)
     * @param customOAuth2User 현재 인증된 사용자의 OAuth2 정보를 포함하는 Principal 객체.
     * @return 페이징된 JD 목록과 API 응답 상태를 포함하는 ResponseDto
     */
    @Operation(summary = "모든 분석 목록 조회", description = "모든 분석 목록을 페이징하여 조회합니다")
    @GetMapping("/jds")
    public ResponseEntity<HttpApiResponse<PagedJdResponseDto>> getPaginatedJds(
            @PageableDefault(
                    size = 11,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(defaultValue = "normal") String showOnly,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User
    ) {
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        jdService.getAllJds(
                                customOAuth2User.getMember(),
                                pageable,
                                showOnly),
                        "나의 분석 내용 전체 조회 성공",
                        HttpStatus.OK
                )
        );
    }


    @Operation(summary = "특정 분석 즐겨찾기 설정", description = "jd_id를 통해 분석을 즐겨찾기로 설정/미설정합니다")
    @PatchMapping("/jds/{jd_id}/bookmark")
    public ResponseEntity<HttpApiResponse<BookmarkResponseDto>> toggleBookmark
            (@PathVariable("jd_id") Long jdId,
             @RequestBody BookmarkRequestDto dto,
             @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        jdService.updateBookmarkStatus(jdId, dto, customOAuth2User.getMember()),
                        "즐겨찾기 설정 완료",
                        HttpStatus.OK
                )
        );
    }

    @Operation(summary = "특정 분석 지원 완료 설정", description = "jd_id를 통해 분석된 채용공고에 지원을 완료/미완료로 설정합니다")
    @PatchMapping("/jds/{jd_id}/apply")
    public ResponseEntity<HttpApiResponse<ApplyStatusResponseDto>> toggleApplyStatus(
            @PathVariable("jd_id") Long jdId,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        ApplyStatusResponseDto response = jdService.toggleApplyStatus(jdId, customOAuth2User.getMember());
        String message = (response.getApplyAt() != null) ? "지원 완료 성공" : "지원 완료 취소 성공";
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        response,
                        message,
                        HttpStatus.OK
                )
        );
    }

    @Operation(summary = "특정 분석 메모 수정", description = "jd_id를 통해 분석의 메모를 수정합니다")
    @PatchMapping("/jds/{jd_id}/memo")
    public ResponseEntity<HttpApiResponse<MemoResponseDto>> updateMemo(
            @PathVariable("jd_id") Long jdId,
            @RequestBody MemoRequestDto dto,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        jdService.updateMemo(jdId, dto, customOAuth2User.getMember()),
                        "메모 수정 성공",
                        HttpStatus.OK
                )
        );
    }

    @Operation(summary = "특정 채용공고 수정", description = "jd_id와 요청받은 Jd를 통해 Jd를 수정합니다")
    @PatchMapping("/jds/{jd_id}")
    public ResponseEntity<HttpApiResponse<JDResponseDto>> updateJd
            (@PathVariable("jd_id") Long jdId,
             @RequestBody JDUpdateRequestDto dto,
             @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        jdService.updateJd(jdId, dto, customOAuth2User.getMember()),
                        "JD 수정 완료",
                        HttpStatus.OK
                )
        );
    }
}
