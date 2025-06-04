package com.zb.jogakjogak.resume.service;

import com.zb.jogakjogak.resume.domain.ResumeRequestDto;
import com.zb.jogakjogak.resume.domain.ResumeResponseDto;
import com.zb.jogakjogak.resume.entity.Resume;
import com.zb.jogakjogak.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;

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
}
