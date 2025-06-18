package com.zb.jogakjogak.jobDescription.controller;

import com.zb.jogakjogak.global.HttpApiResponse;
import com.zb.jogakjogak.jobDescription.domain.requestDto.JDAlarmRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.JDRequestDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.*;
import com.zb.jogakjogak.jobDescription.service.JDService;
import com.zb.jogakjogak.security.dto.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class JDController {

    private final JDService jdService;

    /**
     * open ai를 이용하여 JD와 이력서를 분석하여 To Do List를 만들어주는 컨트롤러 메서드
     *
     * @param jdRequestDto 제목, JD의 URL, 마감일
     * @return 제목, JD의 URL, To Do List, 사용자 메모, 마감일
     */
    @PostMapping("/jd")
    public ResponseEntity<HttpApiResponse<JDResponseDto>> requestSend(
            @RequestBody JDRequestDto jdRequestDto,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        return ResponseEntity.ok(
                new HttpApiResponse<>(jdService.analyze(jdRequestDto, customOAuth2User.getName()),
                        "JD 분석하기 완료", HttpStatus.OK)
        );
    }

    /**
     * gemini ai를 이용하여 JD와 이력서를 분석하여 To Do List를 만들어주는 컨트롤러 메서드
     *
     * @param jdRequestDto 제목, JD의 URL, 마감일
     * @return 제목, JD의 URL, To Do List, 사용자 메모, 마감일
     */
    @PostMapping("/jds")
    public ResponseEntity<HttpApiResponse<JDResponseDto>> llmAnalyze(
            @RequestBody JDRequestDto jdRequestDto,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        String memberName = customOAuth2User.getName();
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        jdService.llmAnalyze(jdRequestDto, memberName),
                        "JD 분석하기 완료",
                        HttpStatus.CREATED
                )
        );
    }

    /**
     * JD 분석 내용 단건 조회하는 컨트롤러 메서드
     *
     * @param jdId 조회하려는 jd의 아이디
     * @return 조회된 jd의 응답 dto
     */
    @GetMapping("/jds/{jd_id}")
    public ResponseEntity<HttpApiResponse<JDResponseDto>> getJd(
            @PathVariable("jd_id") Long jdId) {
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        jdService.getJd(jdId),
                        "나의 분석 내용 단일 조회 완료",
                        HttpStatus.OK
                )
        );
    }

    /**
     * JD 알림 설정을 끄고 키는 메서드
     *
     * @param jdId 알림 설정하려는 jd의 아이디
     * @return 알림 설정을 변경한 JD 응답 dto
     */
    @PatchMapping("/jds/{jd_id}/alarm")
    public ResponseEntity<HttpApiResponse<JDAlarmResponseDto>> alarm(
            @PathVariable Long jdId,
            @RequestBody JDAlarmRequestDto dto) {
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        jdService.alarm(jdId, dto),
                        "알람 설정 완료",
                        HttpStatus.OK
                )
        );
    }

    /**
     * 선택한 JD를 삭제하는 메서드
     *
     * @param jdId 삭제하려는 JD의 아이디
     * @return 삭제된 JD의 응답 Dto
     */
    @DeleteMapping("/jds/{jd_id}")
    public ResponseEntity<HttpApiResponse<JDDeleteResponseDto>> deleteJd(
            @PathVariable Long jdId) {
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        jdService.deleteJd(jdId),
                        "나의 분석 내용 삭제 성공",
                        HttpStatus.OK
                )
        );
    }

    /**
     * 현재 인증된 사용자의 모든 JD (Job Description) 목록을 페이징하여 조회합니다.
     * 기본적으로 한 페이지에 11개의 항목이 표시되며, 생성일(createdAt)을 기준으로 최신순으로 정렬됩니다.
     * 클라이언트가 'page', 'size', 'sort' 파라미터를 통해 페이징 및 정렬 조건을 직접 지정할 수도 있습니다.
     *
     * @param pageable         페이징 및 정렬 정보를 담는 객체.
     * - size: 한 페이지당 항목 수 (기본값 11)
     * - sort: 정렬 기준 필드 (기본값 "createdAt")
     * - direction: 정렬 방향 (기본값 DESC, 즉 최신순)
     * @param customOAuth2User 현재 인증된 사용자의 OAuth2 정보를 포함하는 Principal 객체.
     * @return                 페이징된 JD 목록과 API 응답 상태를 포함하는 ResponseDto
     */
    @GetMapping("/jds")
    public ResponseEntity<HttpApiResponse<PagedJdResponseDto>> getPaginatedJds(
            @PageableDefault(
                    size = 11,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User
    ) {
        Page<AllGetJDResponseDto> jdsPage = jdService.getAllJds(customOAuth2User.getName(), pageable);
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        new PagedJdResponseDto(jdsPage),
                        "나의 분석 내용 전체 조회 성공",
                        HttpStatus.OK
                )
        );
    }
}
