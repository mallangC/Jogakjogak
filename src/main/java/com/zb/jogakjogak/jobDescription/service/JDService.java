package com.zb.jogakjogak.jobDescription.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.zb.jogakjogak.global.exception.*;
import com.zb.jogakjogak.jobDescription.domain.requestDto.*;
import com.zb.jogakjogak.jobDescription.domain.responseDto.*;
import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.jobDescription.entity.ToDoList;
import com.zb.jogakjogak.jobDescription.repository.JDRepository;
import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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
     */
    public JDResponseDto analyze(JDRequestDto jdRequestDto, Member member) {

        if (member.getResume().getContent() == null) {
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

        return JDResponseDto.fromEntity(savedJd, member);
    }

    /**
     * gemini ai를 이용하여 JD와 이력서를 분석하여 To Do List를 만들어주는 서비스 메서드
     */
    public JDResponseDto llmAnalyze(JDRequestDto jdRequestDto, Member member) {

        long jdCount = jdRepository.findAllJdCountByMemberId(member.getId());

        if (member.getResume() == null && jdCount > 0) {
            throw new ResumeException(ResumeErrorCode.ANALYSIS_ALLOWED_ONCE_WITHOUT_RESUME);
        }
        //TODO: 테스트 후 갯수 변경 필요
        if (memberRepository.countJdByMemberId(member.getId()) >= 20) {
            throw new JDException(JDErrorCode.JD_LIMIT_EXCEEDED);
        }

        String analysisJsonString = llmService.generateTodoListJson(member.getResume().getContent(), jdRequestDto.getContent(), jdRequestDto.getJob());
        List<ToDoListDto> parsedAnalysisResult;
        try {
            parsedAnalysisResult = objectMapper.readValue(analysisJsonString, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new JDException(JDErrorCode.FAILED_JSON_PROCESS);
        }

        JD jd = JD.builder()
                .title(jdRequestDto.getTitle())
                .isBookmark(false)
                .isAlarmOn(false)
                .companyName(jdRequestDto.getCompanyName())
                .job(jdRequestDto.getJob())
                .content(jdRequestDto.getContent())
                .jdUrl(jdRequestDto.getJdUrl())
                .endedAt(jdRequestDto.getEndedAt())
                .memo("")
                .member(member)
                .build();

        for (ToDoListDto dto : parsedAnalysisResult) {
            ToDoList toDoList = ToDoList.fromDto(dto, jd);
            jd.addToDoList(toDoList);
        }
        JD savedJd = jdRepository.save(jd);

        return JDResponseDto.fromEntity(savedJd, member);
    }

    public JDResponseDto getJd(Long jdId, Member member) {

        JD jd = getAuthorizedJd(jdId, member);
        return JDResponseDto.fromEntity(jd, member);
    }

    public void deleteJd(Long jdId, Member member) {

        JD jd = getAuthorizedJd(jdId, member);
        jdRepository.deleteById(jd.getId());
    }

    @Transactional
    public JDAlarmResponseDto alarm(Long jdId, JDAlarmRequestDto dto, Member member) {

        JD jd = getAuthorizedJd(jdId, member);
        if(dto.isAlarmOn()){
            member.setNotificationEnabled(true);
        }
        memberRepository.save(member);
        jd.isAlarmOn(dto.isAlarmOn());
        return JDAlarmResponseDto.builder()
                .isAlarmOn(jd.isAlarmOn())
                .jdId(jd.getId())
                .build();
    }

    /**
     * 특정 사용자의 모든 JD (Job Description) 목록을 페이징하여 조회합니다.
     *
     * @param member   조회할 사용자.
     * @param pageable 페이징 및 정렬 정보를 담는 객체.
     * @return 페이징처리된 목록을 포함하는 객체.
     * @throws AuthException 회원을 찾을 수 없을 경우 발생하는 예외.
     */

    @Transactional(readOnly = true)
    public PagedJdResponseDto getAllJds(Member member,
                                        Pageable pageable,
                                        String showOnly) {
        Page<JD> jdEntitiesPage = jdRepository.findAllJdsByMemberIdWithToDoLists(member.getId(), pageable, showOnly);
        int applyJdCount = 0, completedPiecesCount = 0, totalPiecesCount = 0, perfectJdCount = 0;

        for (JD jd : jdEntitiesPage.getContent()) {
            if (jd.getApplyAt() != null) {
                applyJdCount++;
            }

            int totalCount = jd.getToDoLists().size();
            totalPiecesCount += totalCount;
            int completedCount = (int) jd.getToDoLists().stream()
                    .filter(ToDoList::isDone)
                    .count();
            completedPiecesCount += completedCount;

            if (completedCount == totalCount) {
                perfectJdCount++;
            }
        }

        List<AllGetJDResponseDto> dtos = jdEntitiesPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        int allJdCount = dtos.size();

        Page<AllGetJDResponseDto> page = new PageImpl<>(dtos, pageable, jdEntitiesPage.getTotalElements());

        return new PagedJdResponseDto(page, member.getResume(), allJdCount, applyJdCount,
                completedPiecesCount, totalPiecesCount, perfectJdCount);
    }

    /**
     * JD엔티티를 AllGetJDResponseDto로 변환합니다.
     * 이 과정에서 JD에 연결된 ToDoList의 총 개수와 완료된 개수를 계산하여 DTO에 포함합니다.
     */
    private AllGetJDResponseDto convertToDto(JD jd) {
        long totalPieces = jd.getToDoLists().size();
        long completedPieces = jd.getToDoLists().stream()
                .filter(ToDoList::isDone)
                .count();

        return AllGetJDResponseDto.builder()
                .jd_id(jd.getId())
                .title(jd.getTitle())
                .isBookmark(jd.isBookmark())
                .isAlarmOn(jd.isAlarmOn())
                .companyName(jd.getCompanyName())
                .completed_pieces(completedPieces)
                .total_pieces(totalPieces)
                .applyAt(jd.getApplyAt())
                .createdAt(jd.getCreatedAt())
                .updatedAt(jd.getUpdatedAt())
                .endedAt(jd.getEndedAt())
                .build();
    }

    @Transactional
    public BookmarkResponseDto updateBookmarkStatus(Long jdId, BookmarkRequestDto dto, Member member) {

        JD jd = getAuthorizedJd(jdId, member);

        jd.updateBookmarkStatus(dto.isBookmark());
        return BookmarkResponseDto.builder()
                .jd_id(jdId)
                .isBookmark(jd.isBookmark())
                .build();
    }

    @Transactional
    public ApplyStatusResponseDto toggleApplyStatus(Long jdId, Member member) {

        JD updateJd = getAuthorizedJd(jdId, member);
        if (updateJd.getApplyAt() == null) {
            updateJd.markJdAsApplied();
        } else {
            updateJd.unMarkJdAsApplied();
        }
        return ApplyStatusResponseDto.builder()
                .jd_id(jdId)
                .applyAt(updateJd.getApplyAt())
                .build();
    }

    @Transactional
    public MemoResponseDto updateMemo(Long jdId, MemoRequestDto dto, Member member) {
        JD jd = getAuthorizedJd(jdId, member);
        jd.updateMemo(dto);
        return MemoResponseDto.builder()
                .jd_id(jd.getId())
                .memo(jd.getMemo())
                .build();
    }

    @Transactional
    public JDResponseDto updateJd(Long jdId, JDUpdateRequestDto jdUpdateRequestDto, Member member) {
        JD jd = getAuthorizedJd(jdId, member);
        jd.updateJd(jdUpdateRequestDto);
        return JDResponseDto.fromEntity(jd, member);
    }

    /**
     * Helper method to retrieve a JD and ensure the member has access.
     * JD를 검색하고 회원이 접근 권한이 있는지 확인하는 헬퍼 메서드.
     */
    private JD getAuthorizedJd(Long jdId, Member member) {
        return jdRepository.findJdWithMemberAndToDoListsByIdAndMemberId(jdId, member.getId())
                .orElseThrow(() -> new JDException(JDErrorCode.UNAUTHORIZED_ACCESS));
    }
}
