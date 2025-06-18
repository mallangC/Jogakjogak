package com.zb.jogakjogak.jobDescription.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.zb.jogakjogak.global.exception.*;
import com.zb.jogakjogak.jobDescription.domain.requestDto.BookmarkRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.JDAlarmRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.JDRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.ToDoListDto;
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
import java.util.Objects;
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
     * @param memberName   로그인한 유저
     * @return 제목, JD의 URL, To Do List, 사용자 메모, 마감일
     */
    public JDResponseDto analyze(JDRequestDto jdRequestDto, String memberName) {
        Member member = memberRepository.findByUserName(memberName)
                .orElseThrow(() -> new AuthException(MemberErrorCode.NOT_FOUND_MEMBER));

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

        return JDResponseDto.fromEntity(savedJd);
    }

    /**
     * gemini ai를 이용하여 JD와 이력서를 분석하여 To Do List를 만들어주는 서비스 메서드
     *
     * @param jdRequestDto 제목, JD의 URL, 마감일
     * @param memberName   로그인한 유저
     * @return 제목, JD의 URL, To Do List, 사용자 메모, 마감일
     */
    public JDResponseDto llmAnalyze(JDRequestDto jdRequestDto, String memberName) {

        Member member = memberRepository.findByUserName(memberName)
                .orElseThrow(() -> new AuthException(MemberErrorCode.NOT_FOUND_MEMBER));

        if (member.getResume().getContent() == null) {
            throw new ResumeException(ResumeErrorCode.NOT_FOUND_RESUME);
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

        return JDResponseDto.fromEntity(savedJd);
    }

    /**
     * JD 분석 내용 단건 조회하는 서비스 메서드
     *
     * @param jdId       조회하려는 jd의 아이디
     * @param memberName 로그인한 유저
     * @return 조회된 jd의 응답 dto
     */
    @Transactional(readOnly = true)
    public JDResponseDto getJd(Long jdId, String memberName) {

        JD jd = getAuthorizedJd(jdId, memberName);
        return JDResponseDto.fromEntity(jd);
    }

    /**
     * 선택한 JD를 삭제하는 메서드
     *
     * @param jdId       삭제하려는 JD의 아이디
     * @param memberName 로그인한 유저
     */
    public void deleteJd(Long jdId, String memberName) {

        JD jd = getAuthorizedJd(jdId, memberName);
        jdRepository.deleteById(jd.getId());
    }

    /**
     * JD 알림 설정을 끄고 키는 메서드
     *
     * @param jdId       알림 설정하려는 jd의 아이디
     * @param dto        alarm true/false 정보를 가진 dto
     * @param memberName 로그인한 유저
     * @return 알림 설정을 변경한 JD 응답 dto
     */
    @Transactional
    public JDAlarmResponseDto alarm(Long jdId, JDAlarmRequestDto dto, String memberName) {
        JD jd = getAuthorizedJd(jdId, memberName);

        jd.isAlarmOn(dto.isAlarmOn());
        return JDAlarmResponseDto.builder()
                .isAlarmOn(jd.isAlarmOn())
                .jdId(jd.getId())
                .build();
    }

    /**
     * 특정 사용자의 모든 JD (Job Description) 목록을 페이징하여 조회합니다.
     *
     * @param memberName 조회할 사용자의 이름.
     * @param pageable   페이징 및 정렬 정보를 담는 객체.
     * @return 페이징처리된 목록을 포함하는 객체.
     * @throws AuthException 회원을 찾을 수 없을 경우 발생하는 예외.
     */
    @Transactional(readOnly = true)
    public Page<AllGetJDResponseDto> getAllJds(String memberName, Pageable pageable) {
        Member member = memberRepository.findByUserName(memberName)
                .orElseThrow(() -> new AuthException(MemberErrorCode.NOT_FOUND_MEMBER));
        Page<JD> jdEntitiesPage = jdRepository.findByMemberId(member.getId(), pageable);

        List<AllGetJDResponseDto> dtos = jdEntitiesPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, jdEntitiesPage.getTotalElements());
    }

    /**
     * JD엔티티를 AllGetJDResponseDto로 변환합니다.
     * 이 과정에서 JD에 연결된 ToDoList의 총 개수와 완료된 개수를 계산하여 DTO에 포함합니다.
     *
     * @param jd 변환할 JD 엔티티.
     * @return 변환된 AllGetJDResponseDto 객체.
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
                .companyName(jd.getCompanyName())
                .completed_pieces(completedPieces)
                .total_pieces(totalPieces)
                .applyAt(jd.getApplyAt())
                .createdAt(jd.getCreatedAt())
                .updatedAt(jd.getUpdatedAt())
                .endedAt(jd.getEndedAt())
                .build();
    }

    /**
     * 즐겨찾기 상태를 업데이트하는 메서드.
     *
     * @param jdId       업데이트할 JD의 고유 ID
     * @param dto        업데이트할 즐겨찾기 상태를 담고 있는 dto
     * @param memberName 요청을 보낸 사용자의 고유 이름
     * @return 업데이트된 JD의 즐겨찾기 상태를 포함하는 dto
     */
    @Transactional
    public BookmarkResponseDto updateBookmarkStatus(Long jdId, BookmarkRequestDto dto, String memberName) {
        JD jd = getAuthorizedJd(jdId, memberName);
        jd.updateBookmarkStatus(dto.isBookmark());
        JD saveJd = jdRepository.save(jd);
        return BookmarkResponseDto.builder()
                .jd_id(jdId)
                .isBookmark(saveJd.isBookmark())
                .build();
    }

    /**
     * 지원 완료 상태를 토글하는 비즈니스 로직을 수행합니다
     *
     * @param jdId       상태를 토글할 JD의 고유 ID
     * @param memberName 요청을 보낸 사용자의 고유 이름
     * @return 지원 완료 상태를 포함하는 dto
     */
    @Transactional
    public ApplyStatusResponseDto toggleApplyStatus(Long jdId, String memberName) {
        JD updateJd = getAuthorizedJd(jdId, memberName);
        if (updateJd.getApplyAt() == null) {
            updateJd.markJdAsApplied();
        } else {
            updateJd.unMarkJdAsApplied();
        }
        jdRepository.save(updateJd);
        return ApplyStatusResponseDto.builder()
                .jd_id(jdId)
                .applyAt(updateJd.getApplyAt())
                .build();
    }

    /**
     * Helper method to retrieve a JD and ensure the member has access.
     * JD를 검색하고 회원이 접근 권한이 있는지 확인하는 헬퍼 메서드.
     *
     * @param jdId       The ID of the JD to retrieve. (조회하려는 JD의 ID)
     * @param memberName The username of the member. (로그인한 사용자 이름)
     * @return The authorized JD object. (권한이 확인된 JD 객체)
     * @throws AuthException If the member is not found. (회원을 찾을 수 없을 때 발생하는 예외)
     * @throws JDException   If the JD is not found or the member is unauthorized. (JD를 찾을 수 없거나 사용자가 권한이 없을 때 발생하는 예외)
     */
    private JD getAuthorizedJd(Long jdId, String memberName) {
        Member member = memberRepository.findByUserName(memberName)
                .orElseThrow(() -> new AuthException(MemberErrorCode.NOT_FOUND_MEMBER));

        JD jd = jdRepository.findById(jdId)
                .orElseThrow(() -> new JDException(JDErrorCode.JD_NOT_FOUND));

        if (!Objects.equals(member.getId(), jd.getMember().getId())) {
            throw new JDException(JDErrorCode.UNAUTHORIZED_ACCESS);
        }
        return jd;
    }
}
