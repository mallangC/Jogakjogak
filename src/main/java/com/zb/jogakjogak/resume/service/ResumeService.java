package com.zb.jogakjogak.resume.service;

import com.zb.jogakjogak.global.exception.AuthException;
import com.zb.jogakjogak.global.exception.MemberErrorCode;
import com.zb.jogakjogak.global.exception.ResumeException;
import com.zb.jogakjogak.resume.domain.requestDto.ResumeRequestDto;
import com.zb.jogakjogak.resume.domain.responseDto.ResumeResponseDto;
import com.zb.jogakjogak.resume.entity.Resume;
import com.zb.jogakjogak.resume.repository.ResumeRepository;
import com.zb.jogakjogak.security.entity.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.zb.jogakjogak.global.exception.ResumeErrorCode.UNAUTHORIZED_ACCESS;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;

    public ResumeResponseDto register(ResumeRequestDto requestDto, Member member) {

        if (member.getResume() != null) {
            throw new AuthException(MemberErrorCode.ALREADY_HAVE_RESUME);
        }

        Resume newResume = Resume.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .member(member)
                .build();

        Resume resume = resumeRepository.save(newResume);
        return ResumeResponseDto.builder()
                .resumeId(resume.getId())
                .title(resume.getTitle())
                .content(resume.getContent())
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt())
                .build();
    }

    @Transactional
    public ResumeResponseDto modify(Long resumeId, @Valid ResumeRequestDto requestDto, Member member) {

        Resume resume = resumeRepository.findResumeWithMemberByIdAndMemberId(resumeId, member.getId())
                .orElseThrow(() -> new ResumeException(UNAUTHORIZED_ACCESS));

        resume.modify(requestDto);
        Resume savedResume = resumeRepository.save(resume);
        return ResumeResponseDto.builder()
                .resumeId(savedResume.getId())
                .title(savedResume.getTitle())
                .content(savedResume.getContent())
                .createdAt(savedResume.getCreatedAt())
                .updatedAt(savedResume.getUpdatedAt())
                .build();
    }

    public ResumeResponseDto get(Long resumeId, Member member) {

        Resume resume = resumeRepository.findResumeWithMemberByIdAndMemberId(resumeId, member.getId())
                .orElseThrow(() -> new ResumeException(UNAUTHORIZED_ACCESS));
        return ResumeResponseDto.builder()
                .resumeId(resume.getId())
                .title(resume.getTitle())
                .content(resume.getContent())
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt())
                .build();
    }

    @Transactional
    public void delete(Long resumeId, Member member) {
        Resume resumeToDelete = resumeRepository.findResumeWithMemberByIdAndMemberId(resumeId, member.getId())
                .orElseThrow(() -> new ResumeException(UNAUTHORIZED_ACCESS));
        resumeRepository.delete(resumeToDelete);
    }
}
