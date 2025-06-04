package com.zb.jogakjogak.resume.service;

import com.zb.jogakjogak.global.exception.ResumeException;
import com.zb.jogakjogak.resume.domain.ResumeRequestDto;
import com.zb.jogakjogak.resume.domain.ResumeResponseDto;
import com.zb.jogakjogak.resume.entity.Resume;
import com.zb.jogakjogak.resume.repository.ResumeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.zb.jogakjogak.global.exception.ResumeErrorCode.NOT_FOUND_RESUME;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;

    /**
     * 이력서 등록을 위한 서비스 레이어 메서드
     * @param requestDto 이력서 이름, 이력서 내용
     * @return 이력서 id, 이력서 이름, 이력서 번호
     */
    public ResumeResponseDto register(ResumeRequestDto requestDto) {
        Resume saveResume = Resume.builder()
                .name(requestDto.getName())
                .content(requestDto.getContent())
                .isBookMark(false)
                .build();

        Resume resume = resumeRepository.save(saveResume);
        return ResumeResponseDto.builder()
                .resumeId(resume.getId())
                .name(resume.getName())
                .content(resume.getContent())
                .build();
    }

    /**
     *
     * @param resumeId
     * @param requestDto
     * @return
     */
    @Transactional
    public ResumeResponseDto modify(Long resumeId, @Valid ResumeRequestDto requestDto) {
        Resume resume = resumeRepository.findById(resumeId)
                        .orElseThrow(
                                () -> new ResumeException(NOT_FOUND_RESUME)
                        );
        resume.modify(requestDto);
        return ResumeResponseDto.builder()
                .resumeId(resume.getId())
                .name(resume.getName())
                .content(resume.getContent())
                .build();
    }
}
