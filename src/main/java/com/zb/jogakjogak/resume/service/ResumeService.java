package com.zb.jogakjogak.resume.service;

import com.zb.jogakjogak.global.exception.AuthException;
import com.zb.jogakjogak.global.exception.MemberErrorCode;
import com.zb.jogakjogak.global.exception.ResumeException;
import com.zb.jogakjogak.resume.domain.requestDto.ResumeRequestDto;
import com.zb.jogakjogak.resume.domain.responseDto.ResumeResponseDto;
import com.zb.jogakjogak.resume.entity.Resume;
import com.zb.jogakjogak.resume.repository.ResumeRepository;
import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.repository.MemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.zb.jogakjogak.global.exception.ResumeErrorCode.UNAUTHORIZED_ACCESS;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final MemberRepository memberRepository;

    /**
     * 이력서 등록을 위한 서비스 레이어 메서드
     *
     * @param requestDto 이력서 이름, 이력서 내용
     * @return 이력서 id, 이력서 이름, 이력서 번호
     */
    public ResumeResponseDto register(ResumeRequestDto requestDto, String username) {
        Member member = getMemberByUsername(username);

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

    /**
     * 사용자가 이력서를 수정할 떄 찾으려는 서비스 메서드
     *
     * @param resumeId   수정하려는 이력서의 id
     * @param requestDto 수정할 이력서의 내용, 수정할 이력서의 이름
     * @return 수정된 이력서의 id, 수정된 이력서의 이름, 수정된 이력서의 내용
     */
    @Transactional
    public ResumeResponseDto modify(Long resumeId, @Valid ResumeRequestDto requestDto, String username) {

        Member member = getMemberByUsername(username);

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

    /**
     * 사용자가 이력서를 조회할 떄 사용하는 서비스 메서드
     *
     * @param resumeId 찾으려는 이력서의 id
     * @return 사용자가 찾으려는 이력서의 정보
     */
    public ResumeResponseDto get(Long resumeId, String username) {

        Member member = getMemberByUsername(username);

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

    /**
     * 사용자가 이력서를 삭제할 떄 사용하는 서비스 메서드
     *
     * @param resumeId 삭제하려는 이력서의 id
     */
    @Transactional
    public void delete(Long resumeId, String username) {
        Member member = getMemberByUsername(username);
        Resume resumeToDelete = resumeRepository.findResumeWithMemberByIdAndMemberId(resumeId, member.getId())
                .orElseThrow(() -> new ResumeException(UNAUTHORIZED_ACCESS));
        resumeRepository.delete(resumeToDelete);
    }

    /**
     * 사용자 이름을 통해 Member 엔티티를 조회하고, 존재하지 않으면 예외를 발생시킵니다.
     *
     * @param username 조회할 사용자의 이름
     * @return 조회된 Member 엔티티
     */
    private Member getMemberByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException(MemberErrorCode.NOT_FOUND_MEMBER));
    }
}
