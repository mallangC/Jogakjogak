package com.zb.jogakjogak.resume.service;

import com.zb.jogakjogak.global.exception.AuthException;
import com.zb.jogakjogak.global.exception.MemberErrorCode;
import com.zb.jogakjogak.global.exception.ResumeException;
import com.zb.jogakjogak.resume.domain.requestDto.ResumeRequestDto;
import com.zb.jogakjogak.resume.domain.responseDto.ResumeDeleteResponseDto;
import com.zb.jogakjogak.resume.domain.responseDto.ResumeResponseDto;
import com.zb.jogakjogak.resume.entity.Resume;
import com.zb.jogakjogak.resume.repository.ResumeRepository;
import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.repository.MemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.zb.jogakjogak.global.exception.ResumeErrorCode.NOT_FOUND_RESUME;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final MemberRepository memberRepository;

    /**
     * 이력서 등록을 위한 서비스 레이어 메서드
     * @param requestDto 이력서 이름, 이력서 내용
     * @return 이력서 id, 이력서 이름, 이력서 번호
     */
    public ResumeResponseDto register(ResumeRequestDto requestDto, String username) {
        Member member = memberRepository.findByUserName(username);
        if(member == null){
            throw new AuthException(MemberErrorCode.NOT_FOUND_MEMBER);
        }

        if (member.getResume() != null) {
            throw new AuthException(MemberErrorCode.ALREADY_HAVE_RESUME);
        }

        Resume saveResume = Resume.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .member(member)
                .build();

        Resume resume = resumeRepository.save(saveResume);
        return ResumeResponseDto.builder()
                .resumeId(resume.getId())
                .title(resume.getTitle())
                .content(resume.getContent())
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt())
                .build();
    }

     /** 사용자가 이력서를 수정할 떄 찾으려는 서비스 메서드
     * @param resumeId 수정하려는 이력서의 id
     * @param requestDto 수정할 이력서의 내용, 수정할 이력서의 이름
     * @return 수정된 이력서의 id, 수정된 이력서의 이름, 수정된 이력서의 내용
     */
    @Transactional
    public ResumeResponseDto modify(Long resumeId, @Valid ResumeRequestDto requestDto) {
        Resume resume = findResume(resumeId);
        resume.modify(requestDto);
        return ResumeResponseDto.builder()
                .resumeId(resume.getId())
                .title(resume.getTitle())
                .content(resume.getContent())
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt())
                .build();
    }

    /**
     * 사용자가 이력서를 조회할 떄 사용하는 서비스 메서드
     * @param resumeId 찾으련느 이력서의 id
     * @return 사용자가 찾으려는 이력서의 정보
     */
    public ResumeResponseDto get(Long resumeId) {
        Resume resume = findResume(resumeId);
        return ResumeResponseDto.builder()
                .resumeId(resume.getId())
                .title(resume.getTitle())
                .content(resume.getContent())
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt())
                .build();
    }

    /**
     * 이력서의 id로 이력서를 찾는 메서드
     * @param resumeId 찾으려는 이력서의 id
     * @return 찾는 이력서의 정보가 담긴 Resume 객체
     */
    private Resume findResume(Long resumeId) {
        return resumeRepository.findById(resumeId)
                .orElseThrow(
                        () -> new ResumeException(NOT_FOUND_RESUME)
                );
    }

    public ResumeDeleteResponseDto delete(Long resumeId) {
        Resume resume = findResume(resumeId);
        resumeRepository.delete(resume);
        return ResumeDeleteResponseDto.builder()
                .resumeId(resume.getId())
                .build();
    }
}
