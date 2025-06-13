package com.zb.jogakjogak.jobDescription.controller;

import com.zb.jogakjogak.global.HttpApiResponse;
import com.zb.jogakjogak.jobDescription.domain.requestDto.JDAlarmRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.JDRequestDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.JDAlarmResponseDto;
b.jogakjogak.jobDescription.domain.responseDto.JDDeleteResponseDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.JDResponseDto;
import com.zb.jogakjogak.jobDescription.service.JDService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class JDController {

    private final JDService jdService;

    /**
     * open ai를 이용하여 JD와 이력서를 분석하여 To Do List를 만들어주는 컨트롤러 메서드
     * @param jdRequestDto 제목, JD의 URL, 마감일
     * @return 제목, JD의 URL, To Do List, 사용자 메모, 마감일
     */
    @PostMapping("/jd")
    public ResponseEntity<HttpApiResponse<JDResponseDto>> requestSend(@RequestBody JDRequestDto jdRequestDto) {
        return ResponseEntity.ok(new HttpApiResponse<>(jdService.analyze(jdRequestDto), "JD 분석하기 완료", HttpStatus.OK));
    }

    /**
     * gemini ai를 이용하여 JD와 이력서를 분석하여 To Do List를 만들어주는 컨트롤러 메서드
     * @param jdRequestDto 제목, JD의 URL, 마감일
     * @return 제목, JD의 URL, To Do List, 사용자 메모, 마감일
     */
    @PostMapping("/jds")
    public ResponseEntity<HttpApiResponse<JDResponseDto>> llmAnalyze(@RequestBody JDRequestDto jdRequestDto) {
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        jdService.llmAnalyze(jdRequestDto),
                        "JD 분석하기 완료",
                        HttpStatus.CREATED
                )
        );
    }

    /**
     * JD 분석 내용 단건 조회하는 컨트롤러 메서드
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

    /**
     * 선택한 JD를 삭제하는 메서드
     * @param jdId 삭제하려는 JD의 아이디
     * @return 삭제된 JD의 응답 Dto
     */
    @DeleteMapping("/jds/{jd_id}")
    public ResponseEntity<HttpApiResponse<JDDeleteResponseDto>> deleteJd(
            @PathVariable Long jdId ) {
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        jdService.deleteJd(jdId),
                        "나의 분석 내용 삭제 성공",
                        HttpStatus.OK
                )
        );
    }
}
