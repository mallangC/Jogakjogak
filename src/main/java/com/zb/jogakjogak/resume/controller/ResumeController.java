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

    /**
     * 이력서 수정을 위한 컨트롤러 메서드
     * @param resume_id 수정하려는 이력서의 id
     * @param requestDto 수정할 이력서 이름, 수정할 이력서 내용
     * @return data(수정한 이력서 id, 수정된 이력서 이름, 수정된 이력서 내용), 성공 여부 메세지, 상태코드
     */
    @PatchMapping("/{resume_id}")
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
    /**
     * 사용자가 작성한 이력서를 조회하는 컨트롤러 메서드
     * @param resume_id 찾으려는 이력서의 id
     * @return 찾으려는 이력서의 data, 성공 여부 메세지, 상태코드
     */
    @GetMapping("/{resume_id}")
    public ResponseEntity get(@PathVariable Long resume_id) {
        return ResponseEntity.ok()
                .body(
                        new HttpApiResponse(
                                resumeService.get(resume_id),
                                "이력서 조회 성공",
                                HttpStatus.OK
                        )
                );
    }
}
