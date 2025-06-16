package com.zb.jogakjogak.jobDescription.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.zb.jogakjogak.global.exception.*;
import com.zb.jogakjogak.jobDescription.domain.requestDto.JDAlarmRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.JDRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.ToDoListDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.AllGetJDResponseDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.JDAlarmResponseDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.JDDeleteResponseDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.JDResponseDto;
import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.jobDescription.entity.ToDoList;
import com.zb.jogakjogak.jobDescription.repsitory.JDRepository;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JDService {

    private final OpenAIResponseService openAIResponseService;
    private final ObjectMapper objectMapper;
    private final JDRepository jdRepository;
    private final MemberRepository memberRepository;
    private final LLMService llmService;

    /**
     * open ai를 이용하여 JD와 이력서를 분석하여 To Do List를 만들어주는 서비스 메서드
     *
     * @param jdRequestDto 제목, JD의 URL, 마감일
     * @return 제목, JD의 URL, To Do List, 사용자 메모, 마감일
     */
    public JDResponseDto analyze(JDRequestDto jdRequestDto, String memberName) {
        Member member = memberRepository.findByUserName(memberName);
        if(member == null) {
            throw new AuthException(MemberErrorCode.NOT_FOUND_MEMBER);
        }

        if(member.getResume().getContent() == null){
            throw new ResumeException(ResumeErrorCode.NOT_FOUND_RESUME);
        }

        String analysisJsonString = openAIResponseService.sendRequest(member.getResume().getContent(), jdRequestDto.getContent(), 4000);
        List<ToDoListDto> parsedAnalysisResult;
        try {
            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, ToDoListDto.class);
            parsedAnalysisResult = objectMapper.readValue(analysisJsonString, listType);
        } catch (JsonProcessingException e) {
            throw new JDException(JDErrorCode.FAILED_JSON_PROCESS);
        }

        JD jd = JD.builder()
                .title(jdRequestDto.getTitle())
                .jdUrl(jdRequestDto.getJdUrl())
                .endedAt(jdRequestDto.getEndedAt())
                .memo("")
                .companyName(jdRequestDto.getCompanyName())
                .job(jdRequestDto.getJob())
                .content(jdRequestDto.getContent())
                .isAlarmOn(false)
                .build();

        for (ToDoListDto dto : parsedAnalysisResult) {
            ToDoList toDoList = ToDoList.fromDto(dto, jd);
            jd.addToDoList(toDoList);
        }

        JD savedJd = jdRepository.save(jd);

        return JDResponseDto.fromEntity(savedJd);
    }

    /**
     * gemini ai를 이용하여 JD와 이력서를 분석하여 To Do List를 만들어주는 서비스 메서드
     *
     * @param jdRequestDto 제목, JD의 URL, 마감일
     * @return 제목, JD의 URL, To Do List, 사용자 메모, 마감일
     */
    public JDResponseDto llmAnalyze(JDRequestDto jdRequestDto, String memberName) {

        Member member = memberRepository.findByUserName(memberName);
        if (member == null) {
            throw new AuthException(MemberErrorCode.NOT_FOUND_MEMBER);
        }

        if(member.getResume().getContent() == null){
            throw new ResumeException(ResumeErrorCode.NOT_FOUND_RESUME);
        }

        String analysisJsonString = llmService.generateTodoListJson(member.getResume().getContent(), jdRequestDto.getContent());
        List<ToDoListDto> parsedAnalysisResult;
        try {
            parsedAnalysisResult = objectMapper.readValue(analysisJsonString, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new JDException(JDErrorCode.FAILED_JSON_PROCESS);
        }

        JD jd = JD.builder()
                .title(jdRequestDto.getTitle())
                .companyName(jdRequestDto.getCompanyName())
                .job(jdRequestDto.getJob())
                .content(jdRequestDto.getContent())
                .jdUrl(jdRequestDto.getJdUrl())
                .endedAt(jdRequestDto.getEndedAt())
                .memo("")
                .member(member)
                .build();

        for (ToDoListDto dto : parsedAnalysisResult) {
            if (dto.getCategory() == null) {
                System.err.println("경고: LLM 응답에서 ToDoList category가 누락되었습니다. 기본값으로 설정합니다.");
                dto.setCategory(ToDoListType.STRUCTURAL_COMPLEMENT_PLAN);
            }
            if (dto.getTitle() == null || dto.getTitle().isEmpty()) {
                System.err.println("경고: LLM 응답에서 ToDoList title이 누락되었습니다. 기본값으로 설정합니다.");
                dto.setTitle("제목 없음");
            }
            if (dto.getContent() == null || dto.getContent().isEmpty()) {
                System.err.println("경고: LLM 응답에서 ToDoList content가 누락되었습니다. 기본값으로 설정합니다.");
                dto.setContent("내용 없음");
            }

            ToDoList toDoList = ToDoList.fromDto(dto, jd);
            jd.addToDoList(toDoList);
        }
        JD savedJd = jdRepository.save(jd);

        return JDResponseDto.fromEntity(savedJd);
    }

    /**
     * JD 분석 내용 단건 조회하는 서비스 메서드
     * @param jdId 조회하려는 jd의 아이디
     * @return 조회된 jd의 응답 dto
     */
    public JDResponseDto getJd(Long jdId) {
        JD jd = jdRepository.findByIdWithToDoLists(jdId)
                .orElseThrow(() -> new JDException(JDErrorCode.JD_NOT_FOUND));
        return JDResponseDto.fromEntity(jd);

    }

    /**
     * 선택한 JD를 삭제하는 메서드
     * @param jdId 삭제하려는 JD의 아이디
     * @return 삭제된 JD의 응답 Dto
     */
    public JDDeleteResponseDto deleteJd(Long jdId) {
        JD jd = jdRepository.findById(jdId).orElseThrow(
                () -> new JDException(JDErrorCode.JD_NOT_FOUND)
        );
        jdRepository.deleteById(jdId);
        return JDDeleteResponseDto.builder()
                .jd_id(jdId)
                .build();
    }

    /**
     * JD 알림 설정을 끄고 키는 메서드
     * @param jdId 알림 설정하려는 jd의 아이디
     * @return 알림 설정을 변경한 JD 응답 dto
     */
    @Transactional
    public JDAlarmResponseDto alarm(Long jdId, JDAlarmRequestDto dto) {
        JD jd = jdRepository.findById(jdId)
                .orElseThrow(() -> new JDException(JDErrorCode.JD_NOT_FOUND));

        jd.isAlarmOn(dto.isAlarmOn());
        return JDAlarmResponseDto.builder()
                .isAlarmOn(jd.isAlarmOn())
                .jdId(jd.getId())
                .build();
    }

    public Page<AllGetJDResponseDto> getAllJds(String memberName, Pageable pageable) {
        Member member = memberRepository.findByUserName(memberName);
        if (member == null) {
            throw new AuthException(MemberErrorCode.NOT_FOUND_MEMBER);
        }
        Page<JD> jdEntitiesPage = jdRepository.findByMemberId(member.getId(), pageable);

        List<AllGetJDResponseDto> dtos = jdEntitiesPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, jdEntitiesPage.getTotalElements());
    }

    private AllGetJDResponseDto convertToDto(JD jd) {
        long totalPieces = jd.getToDoLists().size();
        long completedPieces = jd.getToDoLists().stream()
                .filter(ToDoList::isDone)
                .count();

        return AllGetJDResponseDto.builder()
                .jd_id(jd.getId())
                .title(jd.getTitle())
                .companyName(jd.getCompanyName())
                .completed_pieces(completedPieces)
                .total_pieces(totalPieces)
                .applyAt(jd.getApplyAt())
                .createdAt(jd.getCreatedAt())
                .updatedAt(jd.getUpdatedAt())
                .endedAt(jd.getEndedAt())
                .build();
    }
}
