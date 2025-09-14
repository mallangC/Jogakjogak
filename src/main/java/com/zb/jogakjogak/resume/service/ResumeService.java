package com.zb.jogakjogak.resume.service;

import com.zb.jogakjogak.global.exception.AuthException;
import com.zb.jogakjogak.global.exception.MemberErrorCode;
import com.zb.jogakjogak.global.exception.ResumeException;
import com.zb.jogakjogak.resume.domain.requestDto.ResumeAddRequestDto;
import com.zb.jogakjogak.resume.domain.requestDto.ResumeRequestDto;
import com.zb.jogakjogak.resume.domain.responseDto.ResumeGetResponseDto;
import com.zb.jogakjogak.resume.domain.responseDto.ResumeResponseDto;
import com.zb.jogakjogak.resume.entity.Career;
import com.zb.jogakjogak.resume.entity.Education;
import com.zb.jogakjogak.resume.entity.Resume;
import com.zb.jogakjogak.resume.entity.Skill;
import com.zb.jogakjogak.resume.repository.CareerRepository;
import com.zb.jogakjogak.resume.repository.EducationRepository;
import com.zb.jogakjogak.resume.repository.ResumeRepository;
import com.zb.jogakjogak.resume.repository.SkillRepository;
import com.zb.jogakjogak.security.entity.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.zb.jogakjogak.global.exception.ResumeErrorCode.*;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final CareerRepository careerRepository;
    private final EducationRepository educationRepository;
    private final SkillRepository skillRepository;

    /**
     * 이력서 등록을 위한 서비스 레이어 메서드
     *
     * @param requestDto 이력서 이름, 이력서 내용
     * @return 이력서 id, 이력서 이름, 이력서 번호
     */

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


    /**
     * (v2)이력서 등록을 위한 서비스 레이어 메서드
     *
     * @param requestDto 이력서 내용, 신입 유무, 경력 리스트, 학력 리스트, 스킬 리스트
     * @return 이력서 id, 이력서 내용, 신입 유무, 경력 리스트, 학력 리스트, 스킬 리스트, 생성 일시, 수정 일시
     */
    @Transactional
    public ResumeGetResponseDto registerV2(ResumeAddRequestDto requestDto, Member member) {

        Resume memberResume = member.getResume();

        if (memberResume != null) {
            throw new AuthException(MemberErrorCode.ALREADY_HAVE_RESUME);
        }

        if (!requestDto.getIsNewcomer() && requestDto.getCareerList() == null) {
            throw new ResumeException(NOT_ENTERED_CAREER);
        }

        Resume newResume = Resume.builder()
                .content(requestDto.getContent())
                .member(member)
                .isNewcomer(requestDto.getIsNewcomer())
                .build();

        Resume saveResume = resumeRepository.save(newResume);

        return saveResumeDetails(saveResume, requestDto);
    }

    public ResumeGetResponseDto getResumeV2(Member member) {
        Resume resume = resumeRepository.findResumeWithCareerAndEducationAndSkill(member.getId())
                .orElseThrow(() -> new ResumeException(NOT_FOUND_RESUME));

        return ResumeGetResponseDto.of(resume);
    }

    @Transactional
    public ResumeGetResponseDto modifyV2(ResumeAddRequestDto requestDto, Member member) {
        if (member.getResume() == null) {
            throw new ResumeException(NOT_FOUND_RESUME);
        }

        if (!requestDto.getIsNewcomer() && requestDto.getCareerList() == null) {
            throw new ResumeException(NOT_ENTERED_CAREER);
        }

        Resume resume = resumeRepository.findResumeWithCareerAndEducationAndSkill(member.getId())
                .orElseThrow(() -> new ResumeException(NOT_FOUND_RESUME));
        resume.update(requestDto);
        resumeRepository.save(resume);
        resumeRepository.deleteResumeDetailsById(resume.getId());

        return saveResumeDetails(resume, requestDto);
    }

    @Transactional
    public ResumeGetResponseDto saveResumeDetails(Resume resume, ResumeAddRequestDto requestDto) {
        List<Career> savedCareerList = new ArrayList<>();
        List<Education> savedEducationList = new ArrayList<>();
        List<Skill> savedSkillList = new ArrayList<>();

        if (requestDto.getCareerList() != null) {
            List<Career> careerList = requestDto.getCareerList().stream()
                    .map(dto -> Career.of(dto, resume))
                    .toList();
            savedCareerList = careerRepository.saveAll(careerList);
        }

        if (requestDto.getEducationList() != null) {
            List<Education> educationList = requestDto.getEducationList().stream()
                    .map(dto -> Education.of(dto, resume))
                    .toList();
            savedEducationList = educationRepository.saveAll(educationList);
        }

        if (requestDto.getSkillList() != null) {
            List<Skill> skillList = requestDto.getSkillList().stream()
                    .map(dto -> Skill.of(dto, resume))
                    .toList();
            savedSkillList = skillRepository.saveAll(skillList);
        }
        Long jdId = requestDto.getJdId();

        return ResumeGetResponseDto.of(resume, savedCareerList, savedEducationList, savedSkillList, jdId);
    }
}
