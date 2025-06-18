package com.zb.jogakjogak.jobDescription.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.zb.jogakjogak.global.exception.AuthException;
import com.zb.jogakjogak.global.exception.JDErrorCode;
import com.zb.jogakjogak.global.exception.JDException;
import com.zb.jogakjogak.global.exception.MemberErrorCode;
import com.zb.jogakjogak.jobDescription.domain.requestDto.BookmarkRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.JDAlarmRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.JDRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.ToDoListDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.*;
import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.jobDescription.entity.ToDoList;
import com.zb.jogakjogak.jobDescription.repository.JDRepository;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import com.zb.jogakjogak.resume.entity.Resume;
import com.zb.jogakjogak.security.Role;
import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JDServiceTest {

    @Mock
    private LLMService llmService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private JDService jdService;

    @Mock
    private JDRepository jdRepository;

    @Mock
    private MemberRepository memberRepository;

    private JDRequestDto jdRequestDto;
    private String mockLLMAnalysisJsonString;
    private List<ToDoListDto> mockToDoListDtosForLLM;
    private Faker faker;
    private Member mockMember;
    private Pageable pageable;
    private JD testJd;

    @BeforeEach
    void setUp() {
        faker = new Faker();

        Resume mockResume = Resume.builder()
                .id(1L)
                .content(faker.lorem().paragraph(5))
                .build();

        mockMember = Member.builder()
                .id(1L)
                .userName("testUser")
                .email("test@example.com")
                .password("password123")
                .role(Role.USER)
                .resume(mockResume)
                .build();

        jdRequestDto = JDRequestDto.builder()
                .title("시니어 백엔드 개발자 채용")
                .jdUrl("https://example.com/jd/123")
                .companyName(faker.company().name())
                .job(faker.job().title())
                .content(faker.lorem().paragraph())
                .endedAt(faker.date().future(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .build();

        mockLLMAnalysisJsonString = "[" +
                "  {" +
                "    \"type\": \"STRUCTURAL_COMPLEMENT_PLAN\"," +
                "    \"title\": \"이력서 Java/Spring Boot 경험 강조\"," +
                "    \"description\": \"이력서에 Spring Boot 프로젝트 경험을 구체적으로 서술합니다.\"," +
                "    \"memo\": \"\"," +
                "    \"isDone\": false" +
                "  }," +
                "  {" +
                "    \"type\": \"CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL\"," +
                "    \"title\": \"AWS 클라우드 경험 구체화\"," +
                "    \"description\": \"AWS EC2 배포 경험을 수치와 함께 명확히 기술합니다.\"," +
                "    \"memo\": \"\"," +
                "    \"isDone\": false" +
                "  }" +
                "]";

        ToDoListDto llmDto1 = new ToDoListDto(ToDoListType.STRUCTURAL_COMPLEMENT_PLAN, "이력서 Java/Spring Boot 경험 강조", "이력서에 Spring Boot 프로젝트 경험을 구체적으로 서술합니다.", "", false);
        ToDoListDto llmDto2 = new ToDoListDto(ToDoListType.CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL, "AWS 클라우드 경험 구체화", "AWS EC2 배포 경험을 수치와 함께 명확히 기술합니다.", "", false);
        mockToDoListDtosForLLM = Arrays.asList(llmDto1, llmDto2);

        pageable = PageRequest.of(0, 11, Sort.by("createdAt").descending());

        testJd = JD.builder()
                .id(1L)
                .title("Test Job")
                .isBookmark(false)
                .member(mockMember)
                .applyAt(null)
                .build();
    }

    @Test
    @DisplayName("LLM 분석 서비스 성공 테스트 - JD 및 ToDoList 저장 포함 (Gemini)")
    void llmAnalyze_success() throws JsonProcessingException {
        // given
        when(memberRepository.findByUserName(anyString())).thenReturn(Optional.of(mockMember));
        when(llmService.generateTodoListJson(anyString(), anyString(), anyString()))
                .thenReturn(mockLLMAnalysisJsonString);
        when(objectMapper.readValue(eq(mockLLMAnalysisJsonString), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(mockToDoListDtosForLLM);
        when(jdRepository.save(any(JD.class))).thenAnswer(invocation -> {
            JD originalJd = invocation.getArgument(0);
            return JD.builder()
                    .id(1L)
                    .title(originalJd.getTitle())
                    .isBookmark(originalJd.isBookmark())
                    .companyName(originalJd.getCompanyName())
                    .job(originalJd.getJob())
                    .content(originalJd.getContent())
                    .jdUrl(originalJd.getJdUrl())
                    .endedAt(originalJd.getEndedAt())
                    .memo(originalJd.getMemo())
                    .member(originalJd.getMember())
                    .isAlarmOn(originalJd.isAlarmOn())
                    .applyAt(originalJd.getApplyAt())
                    .toDoLists(originalJd.getToDoLists())
                    .build();
        });

        // when
        JDResponseDto result = jdService.llmAnalyze(jdRequestDto, mockMember.getResume().getContent());

        // then
        assertNotNull(result);
        assertEquals(jdRequestDto.getTitle(), result.getTitle());
        assertEquals(jdRequestDto.getJdUrl(), result.getJdUrl());
        assertEquals(jdRequestDto.getEndedAt(), result.getEndedAt());
        assertNotNull(result.getToDoLists());
        assertFalse(result.getToDoLists().isEmpty());
        assertEquals(mockToDoListDtosForLLM.size(), result.getToDoLists().size());
        assertEquals(mockToDoListDtosForLLM.get(0).getTitle(), result.getToDoLists().get(0).getTitle());
        assertEquals(mockToDoListDtosForLLM.get(0).getContent(), result.getToDoLists().get(0).getContent());
        assertEquals(mockToDoListDtosForLLM.get(0).getCategory(), result.getToDoLists().get(0).getCategory());
        assertEquals(mockToDoListDtosForLLM.get(0).getMemo(), result.getToDoLists().get(0).getMemo());
        assertEquals(mockToDoListDtosForLLM.get(0).isDone(), result.getToDoLists().get(0).isDone());

        // verify
        verify(llmService, times(1)).generateTodoListJson(anyString(), anyString(), anyString());
        verify(objectMapper, times(1)).readValue(eq(mockLLMAnalysisJsonString), any(com.fasterxml.jackson.core.type.TypeReference.class));
        verify(jdRepository, times(1)).save(any(JD.class));

        ArgumentCaptor<JD> jdCaptor = ArgumentCaptor.forClass(JD.class);
        verify(jdRepository).save(jdCaptor.capture());
        JD savedJdSentToRepo = jdCaptor.getValue();
        assertNotNull(savedJdSentToRepo.getToDoLists());
        assertEquals(mockToDoListDtosForLLM.size(), savedJdSentToRepo.getToDoLists().size());
        assertEquals(mockToDoListDtosForLLM.get(0).getTitle(), savedJdSentToRepo.getToDoLists().get(0).getTitle());
    }

    @Test
    @DisplayName("LLM 분석 서비스 JsonProcessingException 발생 시 JDException 던지는지 테스트 (Gemini)")
    void llmAnalyze_failure_jsonProcessingException() throws JsonProcessingException {
        // given
        when(memberRepository.findByUserName(anyString())).thenReturn(Optional.of(mockMember));
        when(llmService.generateTodoListJson(anyString(), anyString(), anyString()))
                .thenReturn("invalid json string from LLM");

        when(objectMapper.readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenThrow(mock(JsonProcessingException.class));

        // when & then
        JDException thrown = assertThrows(JDException.class, () -> jdService.llmAnalyze(jdRequestDto, mockMember.getResume().getContent()));
        assertEquals(JDErrorCode.FAILED_JSON_PROCESS, thrown.getErrorCode());

        // verify
        verify(llmService, times(1)).generateTodoListJson(anyString(), anyString(), anyString());
        verify(objectMapper, times(1)).readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class));
        verify(jdRepository, never()).save(any(JD.class));
    }

    @Test
    @DisplayName("JD 조회 서비스 성공 테스트 - JD 및 ToDoList 함께 조회")
    void getJd_success() {
        // Given
        Long jdId = 1L;
        JD mockJd = JD.builder()
                .id(jdId)
                .title("테스트 JD")
                .companyName("테스트 회사")
                .job("백엔드 개발자")
                .content("테스트 JD 내용")
                .jdUrl("https://test.com/jd/1")
                .memo("테스트 메모")
                .member(mockMember)
                .isAlarmOn(true)
                .endedAt(LocalDate.now().plusDays(10))
                .build();

        ToDoList toDoList1 = ToDoList.builder()
                .id(101L)
                .category(ToDoListType.STRUCTURAL_COMPLEMENT_PLAN)
                .title("테스트 ToDo 1")
                .content("테스트 ToDo 내용 1")
                .memo("투두 메모 1")
                .isDone(false)
                .jd(mockJd)
                .build();
        ToDoList toDoList2 = ToDoList.builder()
                .id(102L)
                .category(ToDoListType.CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL)
                .title("테스트 ToDo 2")
                .content("테스트 ToDo 내용 2")
                .memo("투두 메모 2")
                .isDone(true)
                .jd(mockJd)
                .build();
        mockJd.addToDoList(toDoList1);
        mockJd.addToDoList(toDoList2);
        when(jdRepository.findByIdWithToDoLists(jdId)).thenReturn(Optional.of(mockJd));
        when(memberRepository.findByUserName(mockMember.getName())).thenReturn(Optional.of(mockMember));

        // When
        JDResponseDto result = jdService.getJd(jdId, mockMember.getName());

        // Then
        assertNotNull(result);
        assertEquals(mockJd.getTitle(), result.getTitle());
        assertEquals(mockJd.getCompanyName(), result.getCompanyName());
        assertEquals(mockJd.getJdUrl(), result.getJdUrl());
        assertEquals(mockJd.getMemo(), result.getMemo());
        assertEquals(mockJd.getId(), result.getJd_id());
        assertEquals(mockJd.isAlarmOn(), result.isAlarmOn());
        assertEquals(mockJd.getEndedAt(), result.getEndedAt());
        assertEquals(mockJd.getCreatedAt(), result.getCreatedAt());
        assertEquals(mockJd.getUpdatedAt(), result.getUpdatedAt());

        // ToDoList 검증
        assertNotNull(result.getToDoLists());
        assertFalse(result.getToDoLists().isEmpty());
        assertEquals(2, result.getToDoLists().size());

        ToDoListResponseDto firstToDo = result.getToDoLists().get(0);
        assertEquals(toDoList1.getId(), firstToDo.getChecklist_id());
        assertEquals(toDoList1.getCategory(), firstToDo.getCategory());
        assertEquals(toDoList1.getTitle(), firstToDo.getTitle());
        assertEquals(toDoList1.getContent(), firstToDo.getContent());
        assertEquals(toDoList1.getMemo(), firstToDo.getMemo());
        assertEquals(toDoList1.isDone(), firstToDo.isDone());
        assertEquals(mockJd.getId(), firstToDo.getJdId());

        // Verify
        verify(jdRepository, times(1)).findByIdWithToDoLists(jdId);
    }

    @Test
    @DisplayName("JD 조회 서비스 실패 테스트 - JD를 찾을 수 없음")
    void getJd_notFound() {
        // Given
        Long nonExistentJdId = 999L;
        when(jdRepository.findByIdWithToDoLists(nonExistentJdId)).thenReturn(Optional.empty());
        when(memberRepository.findByUserName(mockMember.getName())).thenReturn(Optional.of(mockMember));

        // When & Then
        JDException thrown = assertThrows(JDException.class, () -> jdService.getJd(nonExistentJdId, mockMember.getName()));
        assertEquals(JDErrorCode.JD_NOT_FOUND, thrown.getErrorCode());

        // Verify
        verify(jdRepository, times(1)).findByIdWithToDoLists(nonExistentJdId);
    }

    @Test
    @DisplayName("JD 삭제 서비스 성공 테스트 - JD 및 연관된 ToDoList 함께 삭제")
    void deleteJd_success() {
        // Given
        Long jdId = 1L;
        JD mockJd = JD.builder()
                .id(jdId)
                .title(faker.book().title())
                .companyName(faker.artist().name())
                .build();

        when(jdRepository.findById(jdId)).thenReturn(Optional.of(mockJd));
        doNothing().when(jdRepository).deleteById(jdId);

        // When
        JDDeleteResponseDto result = jdService.deleteJd(jdId);

        // Then
        assertNotNull(result);
        assertEquals(jdId, result.getJd_id());

        // Verify
        verify(jdRepository, times(1)).findById(jdId);
        verify(jdRepository, times(1)).deleteById(jdId);
    }

    @Test
    @DisplayName("JD 삭제 서비스 실패 테스트 - JD를 찾을 수 없음")
    void deleteJd_notFound() {
        // Given
        Long nonExistentJdId = 999L;
        when(jdRepository.findById(nonExistentJdId)).thenReturn(Optional.empty());

        // When & Then
        JDException thrown = assertThrows(JDException.class, () -> jdService.deleteJd(nonExistentJdId));
        assertEquals(JDErrorCode.JD_NOT_FOUND, thrown.getErrorCode());

        // Verify
        verify(jdRepository, times(1)).findById(nonExistentJdId);
        verify(jdRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("JD 알람 상태 변경 서비스 성공 테스트")
    void alarm_success() {
        // Given
        Long jdId = 1L;
        boolean initialAlarmStatus = false;
        boolean newAlarmStatus = true;

        JDAlarmRequestDto requestDto = JDAlarmRequestDto.builder()
                .isAlarmOn(newAlarmStatus)
                .build();

        JD mockJd = JD.builder()
                .id(jdId)
                .title("알람 테스트 JD")
                .member(mockMember)
                .isAlarmOn(initialAlarmStatus)
                .build();

        when(jdRepository.findById(jdId)).thenReturn(Optional.of(mockJd));
        when(memberRepository.findByUserName(mockMember.getName())).thenReturn(Optional.of(mockMember));

        // When
        JDAlarmResponseDto result = jdService.alarm(jdId, requestDto, mockMember.getName());

        // Then
        assertNotNull(result);
        assertEquals(jdId, result.getJdId());
        assertEquals(newAlarmStatus, result.isAlarmOn());


        verify(jdRepository, times(1)).findById(jdId);
        assertEquals(newAlarmStatus, mockJd.isAlarmOn());
    }

    @Test
    @DisplayName("JD 알람 상태 변경 서비스 실패 테스트 - JD를 찾을 수 없음")
    void alarm_notFound() {
        // Given
        Long nonExistentJdId = 999L;
        JDAlarmRequestDto requestDto = JDAlarmRequestDto.builder()
                .isAlarmOn(true)
                .build();

        when(jdRepository.findById(nonExistentJdId)).thenReturn(Optional.empty());
        when(memberRepository.findByUserName(mockMember.getName())).thenReturn(Optional.of(mockMember));

        // When & Then
        JDException thrown = assertThrows(JDException.class, () -> jdService.alarm(nonExistentJdId, requestDto, mockMember.getName()));
        assertEquals(JDErrorCode.JD_NOT_FOUND, thrown.getErrorCode());

        verify(jdRepository, times(1)).findById(nonExistentJdId);
    }

    @Test
    @DisplayName("JD 목록 성공적으로 조회 및 ToDoList 개수 계산")
    void getAllJds_Success() {
        // Given
        when(memberRepository.findByUserName(mockMember.getUserName())).thenReturn(Optional.of(mockMember));

        ToDoList todo1 = ToDoList.builder()
                .id(1L).category(ToDoListType.STRUCTURAL_COMPLEMENT_PLAN).title("투두1").content("내용1").isDone(true).build();
        ToDoList todo2 = ToDoList.builder()
                .id(2L).category(ToDoListType.CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL).title("투두2").content("내용2").isDone(false).build();
        ToDoList todo3 = ToDoList.builder()
                .id(3L).category(ToDoListType.STRUCTURAL_COMPLEMENT_PLAN).title("투두3").content("내용3").isDone(true).build();

        JD jd1 = JD.builder()
                .id(101L).title("백엔드 개발자").companyName("SKC").endedAt(LocalDate.of(2025, 5, 24))
                .member(mockMember)
                .build();
        jd1.addToDoList(todo1);
        jd1.addToDoList(todo2);

        JD jd2 = JD.builder()
                .id(102L).title("UX 디렉터").companyName("메리츠화재").endedAt(LocalDate.of(2025, 1, 20))
                .member(mockMember)
                .build();
        jd2.addToDoList(todo3);


        List<JD> jds = Arrays.asList(jd1, jd2);
        Page<JD> jdPage = new PageImpl<>(jds, pageable, jds.size());

        when(jdRepository.findByMemberId(mockMember.getId(), pageable)).thenReturn(jdPage);

        // When
        Page<AllGetJDResponseDto> resultPage = jdService.getAllJds(mockMember.getUserName(), pageable);

        // Then
        verify(memberRepository, times(1)).findByUserName(mockMember.getUserName());
        verify(jdRepository, times(1)).findByMemberId(mockMember.getId(), pageable);

        assertNotNull(resultPage);
        assertEquals(2, resultPage.getContent().size());
        assertEquals(2, resultPage.getTotalElements());
        assertEquals(1, resultPage.getTotalPages());

        AllGetJDResponseDto dto1 = resultPage.getContent().get(0);
        assertEquals(101L, dto1.getJd_id());
        assertEquals(2L, dto1.getTotal_pieces());
        assertEquals(1L, dto1.getCompleted_pieces());

        AllGetJDResponseDto dto2 = resultPage.getContent().get(1);
        assertEquals(102L, dto2.getJd_id());
        assertEquals(1L, dto2.getTotal_pieces());
        assertEquals(1L, dto2.getCompleted_pieces());
    }

    @Test
    @DisplayName("JD 목록 조회 시 회원이 존재하지 않으면 AuthException 발생")
    void getAllJds_MemberNotFound_ThrowsAuthException() {
        // Given
        when(memberRepository.findByUserName(anyString())).thenReturn(Optional.empty());

        // When & Then
        AuthException exception = assertThrows(AuthException.class, () ->
                jdService.getAllJds("nonExistentUser", pageable)
        );

        assertEquals(MemberErrorCode.NOT_FOUND_MEMBER, exception.getMemberErrorCode());

        verify(jdRepository, never()).findByMemberId(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("회원은 존재하지만 해당 회원의 JD가 없을 때 빈 페이지 반환")
    void getAllJds_NoJdsForMember_ReturnsEmptyPage() {
        // Given
        when(memberRepository.findByUserName(mockMember.getUserName())).thenReturn(Optional.of(mockMember));

        Page<JD> emptyJdPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(jdRepository.findByMemberId(mockMember.getId(), pageable)).thenReturn(emptyJdPage);

        // When
        Page<AllGetJDResponseDto> resultPage = jdService.getAllJds(mockMember.getUserName(), pageable);

        // Then
        verify(memberRepository, times(1)).findByUserName(mockMember.getUserName());
        verify(jdRepository, times(1)).findByMemberId(mockMember.getId(), pageable);

        assertNotNull(resultPage);
        assertTrue(resultPage.getContent().isEmpty());
        assertEquals(0, resultPage.getTotalElements());
        assertEquals(0, resultPage.getTotalPages());
    }

    @Test
    @DisplayName("북마크 상태 업데이트 성공 - true로 변경")
    void updateBookmarkStatus_success_toTrue() {
        // Given
        BookmarkRequestDto dto = BookmarkRequestDto.builder().isBookmark(true).build();
        when(memberRepository.findByUserName(mockMember.getName())).thenReturn(Optional.of(mockMember));
        when(jdRepository.findById(anyLong())).thenReturn(Optional.of(testJd));
        when(jdRepository.save(any(JD.class))).thenReturn(testJd);

        // When
        BookmarkResponseDto response = jdService.updateBookmarkStatus(testJd.getId(), dto, mockMember.getName());

        // Then
        assertNotNull(response);
        assertEquals(testJd.getId(), response.getJd_id());
        assertTrue(response.isBookmark());
        assertTrue(testJd.isBookmark());

        // Mock 객체의 메서드 호출 검증
        verify(memberRepository, times(1)).findByUserName(mockMember.getName());
        verify(jdRepository, times(1)).findById(testJd.getId());
        verify(jdRepository, times(1)).save(testJd);
    }

    @Test
    @DisplayName("북마크 상태 업데이트 성공 - false로 변경")
    void updateBookmarkStatus_success_toFalse() {
        // Given
        testJd.updateBookmarkStatus(true);
        BookmarkRequestDto dto = BookmarkRequestDto.builder().isBookmark(false).build();
        when(memberRepository.findByUserName(mockMember.getName())).thenReturn(Optional.of(mockMember));
        when(jdRepository.findById(anyLong())).thenReturn(Optional.of(testJd));
        when(jdRepository.save(any(JD.class))).thenReturn(testJd);

        // When
        BookmarkResponseDto response = jdService.updateBookmarkStatus(testJd.getId(), dto, mockMember.getName());

        // Then
        assertNotNull(response);
        assertEquals(testJd.getId(), response.getJd_id());
        assertFalse(response.isBookmark());
        assertFalse(testJd.isBookmark());

        verify(memberRepository, times(1)).findByUserName(mockMember.getName());
        verify(jdRepository, times(1)).findById(testJd.getId());
        verify(jdRepository, times(1)).save(testJd);
    }

    @Test
    @DisplayName("북마크 상태 업데이트 실패 - 멤버를 찾을 수 없음")
    void updateBookmarkStatus_fail_memberNotFound() {
        // Given
        BookmarkRequestDto dto = BookmarkRequestDto.builder().isBookmark(true).build();
        when(memberRepository.findByUserName(mockMember.getName())).thenReturn(Optional.empty());

        // When & Then
        AuthException exception = assertThrows(AuthException.class,
                () -> jdService.updateBookmarkStatus(testJd.getId(), dto, mockMember.getName()));
        assertEquals(MemberErrorCode.NOT_FOUND_MEMBER, exception.getMemberErrorCode());

        verify(memberRepository, times(1)).findByUserName(mockMember.getName());
        verify(jdRepository, never()).findById(anyLong());
        verify(jdRepository, never()).save(any(JD.class));
    }

    @Test
    @DisplayName("북마크 상태 업데이트 실패 - JD를 찾을 수 없음")
    void updateBookmarkStatus_fail_jdNotFound() {
        // Given
        BookmarkRequestDto dto = BookmarkRequestDto.builder().isBookmark(true).build();
        when(memberRepository.findByUserName(mockMember.getName())).thenReturn(Optional.of(mockMember));
        when(jdRepository.findById(testJd.getId())).thenReturn(Optional.empty());

        // When & Then
        JDException exception = assertThrows(JDException.class,
                () -> jdService.updateBookmarkStatus(testJd.getId(), dto, mockMember.getName()));
        assertEquals(JDErrorCode.JD_NOT_FOUND, exception.getErrorCode());

        verify(memberRepository, times(1)).findByUserName(mockMember.getName());
        verify(jdRepository, times(1)).findById(testJd.getId());
        verify(jdRepository, never()).save(any(JD.class));
    }

    @Test
    @DisplayName("북마크 상태 업데이트 실패 - 권한 없음")
    void updateBookmarkStatus_fail_unauthorizedAccess() {
        // Given
        BookmarkRequestDto dto = BookmarkRequestDto.builder().isBookmark(true).build();
        Member requestMember  = Member.builder().id(200L).userName("otherUser").build();
        when(jdRepository.findById(anyLong())).thenReturn(Optional.of(testJd));
        when(memberRepository.findByUserName(requestMember.getUserName())).thenReturn(Optional.of(requestMember));


        // When & Then
        JDException exception = assertThrows(JDException.class,
                () -> jdService.updateBookmarkStatus(testJd.getId(), dto, requestMember.getUserName()));
        assertEquals(JDErrorCode.UNAUTHORIZED_ACCESS, exception.getErrorCode());

        verify(memberRepository, times(1)).findByUserName(requestMember.getUserName().trim());
        verify(jdRepository, times(1)).findById(testJd.getId());
        verify(jdRepository, never()).save(any(JD.class));
    }


    @Test
    @DisplayName("지원 완료 상태 토글 성공 - null에서 현재 시간으로 변경")
    void toggleApplyStatus_success_fromNullToNow() {
        // Given
        when(memberRepository.findByUserName(mockMember.getName())).thenReturn(Optional.of(mockMember));
        when(jdRepository.findById(testJd.getId())).thenReturn(Optional.of(testJd));
        when(jdRepository.save(any(JD.class))).thenReturn(testJd);

        // When
        ApplyStatusResponseDto response = jdService.toggleApplyStatus(testJd.getId(), mockMember.getName());

        // Then
        assertNotNull(response);
        assertEquals(testJd.getId(), response.getJd_id());
        assertNotNull(response.getApplyAt());
        assertNotNull(testJd.getApplyAt());

        verify(memberRepository, times(1)).findByUserName(mockMember.getName());
        verify(jdRepository, times(1)).findById(anyLong());
        verify(jdRepository, times(1)).save(testJd);
    }

    @Test
    @DisplayName("지원 완료 상태 토글 성공 - 현재 시간에서 null로 변경")
    void toggleApplyStatus_success_fromNowToNull() {
        // Given
        testJd.markJdAsApplied();
        when(memberRepository.findByUserName(mockMember.getName())).thenReturn(Optional.of(mockMember));
        when(jdRepository.findById(anyLong())).thenReturn(Optional.of(testJd));
        when(jdRepository.save(any(JD.class))).thenReturn(testJd);

        // When
        ApplyStatusResponseDto response = jdService.toggleApplyStatus(testJd.getId(), mockMember.getName());

        // Then
        assertNotNull(response);
        assertEquals(testJd.getId(), response.getJd_id());
        assertNull(response.getApplyAt());
        assertNull(testJd.getApplyAt());

        verify(memberRepository, times(1)).findByUserName(mockMember.getName());
        verify(jdRepository, times(1)).findById(anyLong());
        verify(jdRepository, times(1)).save(testJd);
    }

    @Test
    @DisplayName("지원 완료 상태 토글 실패 - 멤버를 찾을 수 없음")
    void toggleApplyStatus_fail_memberNotFound() {
        // Given
        when(memberRepository.findByUserName(mockMember.getName())).thenReturn(Optional.empty());

        // When & Then
        AuthException exception = assertThrows(AuthException.class,
                () -> jdService.toggleApplyStatus(testJd.getId(), mockMember.getName()));
        assertEquals(MemberErrorCode.NOT_FOUND_MEMBER, exception.getMemberErrorCode());

        verify(memberRepository, times(1)).findByUserName(mockMember.getName());
        verify(jdRepository, never()).findById(anyLong());
        verify(jdRepository, never()).save(any(JD.class));
    }

    @Test
    @DisplayName("지원 완료 상태 토글 실패 - JD를 찾을 수 없음")
    void toggleApplyStatus_fail_jdNotFound() {
        // Given
        when(memberRepository.findByUserName(mockMember.getName())).thenReturn(Optional.of(mockMember));
        when(jdRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        JDException exception = assertThrows(JDException.class,
                () -> jdService.toggleApplyStatus(testJd.getId(), mockMember.getName()));
        assertEquals(JDErrorCode.JD_NOT_FOUND, exception.getErrorCode());

        verify(memberRepository, times(1)).findByUserName(mockMember.getName());
        verify(jdRepository, times(1)).findById(anyLong());
        verify(jdRepository, never()).save(any(JD.class));
    }

    @Test
    @DisplayName("지원 완료 상태 토글 실패 - 권한 없음")
    void toggleApplyStatus_fail_unauthorizedAccess() {
        // Given
        Member otherMember = Member.builder().id(200L).userName("otherUser").build();
        when(memberRepository.findByUserName(otherMember.getUserName())).thenReturn(Optional.of(otherMember));
        when(jdRepository.findById(anyLong())).thenReturn(Optional.of(testJd));

        // When & Then
        JDException exception = assertThrows(JDException.class,
                () -> jdService.toggleApplyStatus(testJd.getId(), otherMember.getUserName()));
        assertEquals(JDErrorCode.UNAUTHORIZED_ACCESS, exception.getErrorCode());

        verify(memberRepository, times(1)).findByUserName(otherMember.getUserName());
        verify(jdRepository, times(1)).findById(anyLong());
        verify(jdRepository, never()).save(any(JD.class));
    }
}
