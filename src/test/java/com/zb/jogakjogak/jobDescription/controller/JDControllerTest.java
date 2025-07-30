package com.zb.jogakjogak.jobDescription.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.zb.jogakjogak.jobDescription.domain.requestDto.BookmarkRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.JDAlarmRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.JDRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.MemoRequestDto;
import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.jobDescription.entity.ToDoList;
import com.zb.jogakjogak.jobDescription.repository.JDRepository;
import com.zb.jogakjogak.jobDescription.repository.ToDoListRepository;
import com.zb.jogakjogak.jobDescription.service.LLMService;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import com.zb.jogakjogak.resume.entity.Resume;
import com.zb.jogakjogak.resume.repository.ResumeRepository;
import com.zb.jogakjogak.security.Role;
import com.zb.jogakjogak.security.WithMockCustomUser;
import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class JDControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JDRepository jdRepository;

    @Autowired
    private ToDoListRepository toDoListRepository;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private LLMService llmService;

    private final Faker faker = new Faker();

    private final String testUserLoginId = "testUserLoginId";
    private final String testUserRealName = "Test Real User";
    private final String testUserEmail = "test@email.com";

    private final String otherUserLoginId = "otherUser";
    private final String otherUserRealName = "Other User";
    private final String otherUserEmail = "other@example.com";

    private Member setupMember;
    private Member otherMember;
    private Resume mockResume;

    @BeforeEach
    @Transactional
    void setUp() {
        setupMember = Member.builder()
                .name(testUserRealName)
                .phoneNumber("01000000000")
                .username(testUserLoginId)
                .password(faker.internet().password())
                .email(testUserEmail)
                .nickname(faker.animal().name())
                .role(Role.USER)
                .lastLoginAt(LocalDateTime.now())
                .build();
        memberRepository.save(setupMember);

        otherMember = Member.builder()
                .name(otherUserRealName)
                .phoneNumber("01011112222")
                .username(otherUserLoginId)
                .password(faker.internet().password())
                .email(otherUserEmail)
                .nickname(faker.animal().name())
                .role(Role.USER)
                .lastLoginAt(LocalDateTime.now())
                .build();
        memberRepository.save(otherMember);

        mockResume = Resume.builder()
                .member(setupMember)
                .content(faker.lorem().word())
                .title(faker.lorem().word())
                .build();
        resumeRepository.save(mockResume);

        entityManager.clear();
    }

    @AfterEach
    @Transactional
    void tearDown() {
        toDoListRepository.deleteAllInBatch();
        jdRepository.deleteAllInBatch();
        resumeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        entityManager.clear();
        SecurityContextHolder.clearContext();
    }

    /**
     * JD를 DB에 직접 저장하는 유틸리티 메서드 (테스트 데이터 세팅용)
     *
     * @param member       JD를 소유할 멤버
     * @param title        JD 제목
     * @param url          JD URL
     * @param dueDate      마감일
     * @param memo         메모 내용
     * @param isBookmarked 즐겨찾기 여부
     * @param isAlarmOn    알림 설정 여부
     * @param applyAt      지원 완료일 (null이면 미완료)
     * @return 저장된 JD 엔티티
     */
    private JD createAndSaveJd(Member member,
                               String title,
                               String url,
                               String companyName,
                               String content,
                               String job,
                               LocalDateTime dueDate,
                               String memo,
                               boolean isBookmarked,
                               boolean isAlarmOn,
                               LocalDateTime applyAt) {
        JD jd = JD.builder()
                .title(title)
                .companyName(companyName)
                .jdUrl(url)
                .memo(memo)
                .job(job)
                .content(content)
                .endedAt(dueDate)
                .isBookmark(isBookmarked)
                .isAlarmOn(isAlarmOn)
                .applyAt(applyAt)
                .member(member)
                .build();
        JD savedJd = jdRepository.save(jd);

        IntStream.range(0, 3).forEach(i -> {
            ToDoList toDoList = ToDoList.builder()
                    .title("투두리스트 제목" + i)
                    .content("테스트 할 일 " + (i + 1))
                    .category(ToDoListType.STRUCTURAL_COMPLEMENT_PLAN)
                    .isDone(false)
                    .jd(savedJd)
                    .build();
            toDoListRepository.save(toDoList);
        });
        entityManager.clear();
        return savedJd;
    }


    @Test
    @DisplayName("Gemini를 이용한 JD 분석 성공")
    @WithMockCustomUser(username = testUserLoginId, realName = testUserRealName, email = testUserEmail, role = "USER")
    void llmAnalyze_success() throws Exception {
        // Given
        JDRequestDto requestDto = new JDRequestDto(
                "Gemini 테스트 JD",
                "http://gemini.com/jd/1",
                "테스트용 회사 이름",
                "테스트용 직무 이름",
                "채용 공고 내용",
                LocalDateTime.now().plusDays(10)
        );

        String content = objectMapper.writeValueAsString(requestDto);
        String mockTodoListJson = "[{\"category\":\"STRUCTURAL_COMPLEMENT_PLAN\",\"title\":\"테스트 투두1\",\"content\":\"테스트 투두 내용 1입니다.\",\"memo\":\"\",\"isDone\":false}]";
        Mockito.when(llmService.generateTodoListJson(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(mockTodoListJson);

        // When
        ResultActions result = mockMvc.perform(post("/jds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("JD 분석하기 완료"))
                .andExpect(jsonPath("$.data.title").value("Gemini 테스트 JD"))
                .andExpect(jsonPath("$.data.jdUrl").value("http://gemini.com/jd/1"))
                .andExpect(jsonPath("$.data.endedAt").exists())
                .andExpect(jsonPath("$.data.toDoLists").isArray())
                .andExpect(jsonPath("$.data.toDoLists.length()").isNumber())
                .andExpect(jsonPath("$.data.memo").exists())
                .andDo(print());

        // DB에 저장되었는지 확인
        entityManager.clear();
        List<JD> jds = jdRepository.findAllByMember(setupMember);
        assertThat(jds).hasSize(1);
        assertThat(jds.get(0).getTitle()).isEqualTo("Gemini 테스트 JD");
    }

    @Test
    @DisplayName("JD 단건 조회 성공")
    @WithMockCustomUser(username = testUserLoginId, realName = testUserRealName, email = testUserEmail, role = "USER")
    void getJd_success() throws Exception {
        // Given
        JD jd = createAndSaveJd(
                setupMember,
                "조회용 JD",
                "http://get.com/jd/1",
                "테스트용 회사이름",
                "테스트용 채용 공고 내용",
                "테스트용 직무",
                LocalDateTime.now().plusWeeks(1),
                "조회용 메모",
                false,
                false, null);
        Long jdId = jd.getId();

        // When
        ResultActions result = mockMvc.perform(get("/jds/{jd_id}", jdId));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("나의 분석 내용 단일 조회 완료"))
                .andExpect(jsonPath("$.data.jd_id").value(jdId))
                .andExpect(jsonPath("$.data.title").value("조회용 JD"))
                .andExpect(jsonPath("$.data.toDoLists.length()").value(3))
                .andDo(print());
    }

    @Test
    @DisplayName("JD 알림 설정 토글 성공")
    @WithMockCustomUser(username = testUserLoginId, realName = testUserRealName, email = testUserEmail, role = "USER")
    void alarm_success() throws Exception {
        // Given
        JD jd = createAndSaveJd(
                setupMember,
                "조회용 JD",
                "http://get.com/jd/1",
                "테스트용 회사이름",
                "테스트용 채용 공고 내용",
                "테스트용 직무",
                LocalDateTime.now().plusWeeks(1),
                "조회용 메모",
                false,
                false,
                null
        );
        Long jdId = jd.getId();

        // When: 알림 켜기
        JDAlarmRequestDto turnOnRequest = new JDAlarmRequestDto(true);
        ResultActions resultOn = mockMvc.perform(patch("/jds/{jd_id}/alarm", jdId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(turnOnRequest)));

        // Then: 알림 켜짐 확인
        resultOn.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("알람 설정 완료"))
                .andExpect(jsonPath("$.data.jdId").value(jdId))
                .andExpect(jsonPath("$.data.alarmOn").value(true))
                .andDo(print());

        entityManager.clear();
        assertThat(jdRepository.findById(jdId).get().isAlarmOn()).isTrue();

        // When: 알림 끄기
        JDAlarmRequestDto turnOffRequest = new JDAlarmRequestDto(false);
        ResultActions resultOff = mockMvc.perform(patch("/jds/{jd_id}/alarm", jdId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(turnOffRequest)));

        // Then: 알림 꺼짐 확인
        resultOff.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("알람 설정 완료"))
                .andExpect(jsonPath("$.data.jdId").value(jdId))
                .andExpect(jsonPath("$.data.alarmOn").value(false))
                .andDo(print());

        entityManager.clear();
        assertThat(jdRepository.findById(jdId).get().isAlarmOn()).isFalse();
    }

    @Test
    @DisplayName("JD 삭제 성공")
    @WithMockCustomUser(username = testUserLoginId, realName = testUserRealName, email = testUserEmail, role = "USER")
    void deleteJd_success() throws Exception {
        // Given
        JD jdToDelete = createAndSaveJd(
                setupMember,
                "조회용 JD",
                "http://get.com/jd/1",
                "테스트용 회사이름",
                "테스트용 채용 공고 내용",
                "테스트용 직무",
                LocalDateTime.now().plusWeeks(1),
                "조회용 메모",
                false,
                false,
                null
        );
        Long jdId = jdToDelete.getId();

        // When
        ResultActions result = mockMvc.perform(delete("/jds/{jd_id}", jdId));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("나의 분석 내용 삭제 성공"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());

        entityManager.clear();
        assertThat(jdRepository.findById(jdId)).isEmpty();
        assertThat(toDoListRepository.findAllByJdId(jdId)).isEmpty(); // 연관된 ToDo도 삭제되었는지 확인
    }

    @Test
    @DisplayName("JD 목록 페이징 조회 성공 - 기본값")
    @WithMockCustomUser(username = testUserLoginId, realName = testUserRealName, email = testUserEmail, role = "USER")
    void getPaginatedJds_success_default() throws Exception {
        // Given
        // 테스트 사용자의 JD 15개 생성
        IntStream.range(0, 15).forEach(i ->
                createAndSaveJd(
                        setupMember,
                        "JD_" + i,
                        "http://test.com/jd/" + i,
                        "테스트용 회사 이름",
                        "테스트용 채용 공고 내용",
                        "테스트용 직무",
                        LocalDateTime.now().plusDays(i),
                        "",
                        false,
                        false,
                        null
                )
        );
        // 다른 사용자의 JD 5개 생성 (조회되지 않아야 함)
        IntStream.range(0, 5).forEach(i ->
                createAndSaveJd(
                        otherMember,
                        "Other_JD_" + i,
                        "http://other.com/jd/" + i,
                        "테스트용 회사 이름",
                        "테스트용 채용 공고 내용",
                        "테스트용 직무",
                        LocalDateTime.now().plusDays(i),
                        "",
                        false,
                        false,
                        null
                )
        );

        // When
        ResultActions result = mockMvc.perform(get("/jds"));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("나의 분석 내용 전체 조회 성공"))
                .andExpect(jsonPath("$.data.jds").isArray())
                .andExpect(jsonPath("$.data.jds.length()").value(11)) // 기본 size=11
                .andExpect(jsonPath("$.data.totalElements").value(15)) // 총 15개 중
                .andExpect(jsonPath("$.data.totalPages").value(2))   // 2 페이지
                .andExpect(jsonPath("$.data.currentPage").value(0))  // 0 페이지
                .andDo(print());
    }

    @Test
    @DisplayName("JD 목록 페이징 조회 성공 - 커스텀 파라미터")
    @WithMockCustomUser(username = testUserLoginId, realName = testUserRealName, email = testUserEmail, role = "USER")
    void getPaginatedJds_success_custom() throws Exception {
        // Given
        // 테스트 사용자의 JD 15개 생성
        IntStream.range(0, 15).forEach(i ->
                createAndSaveJd(
                        setupMember,
                        "JD_" + i,
                        "http://test.com/jd/" + i,
                        "테스트용 회사 이름",
                        "테스트용 채용 공고 내용",
                        "테스트용 직무",
                        LocalDateTime.now().plusDays(i),
                        "",
                        false,
                        false,
                        null));

        // When (페이지 1, 사이즈 5, 제목 기준 오름차순 정렬)
        ResultActions result = mockMvc.perform(get("/jds")
                .param("page", "1")
                .param("size", "5")
                .param("sort", "title,asc"));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("나의 분석 내용 전체 조회 성공"))
                .andExpect(jsonPath("$.data.jds").isArray())
                .andExpect(jsonPath("$.data.jds.length()").value(5))
                .andExpect(jsonPath("$.data.totalElements").value(15))
                .andExpect(jsonPath("$.data.totalPages").value(3))
                .andExpect(jsonPath("$.data.currentPage").value(1))
                .andDo(print());
    }

    @Test
    @DisplayName("JD 즐겨찾기 토글 성공")
    @WithMockCustomUser(username = testUserLoginId, realName = testUserRealName, email = testUserEmail, role = "USER")
    void toggleBookmark_success() throws Exception {
        // Given
        JD jd = createAndSaveJd(
                setupMember,
                "즐겨찾기 테스트",
                "http://bookmark.com/jd/1",
                "테스트용 회사 이름",
                "테스트용 채용 공고 내용",
                "테스트용 직무",
                LocalDateTime.now(),
                "",
                false,
                false,
                null);
        Long jdId = jd.getId();

        // When: 즐겨찾기 설정 (true)
        BookmarkRequestDto setBookmark = new BookmarkRequestDto(true);
        ResultActions resultOn = mockMvc.perform(patch("/jds/{jd_id}/bookmark", jdId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(setBookmark)));

        // Then: 즐겨찾기 설정 확인
        resultOn.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("즐겨찾기 설정 완료"))
                .andExpect(jsonPath("$.data.jd_id").value(jdId))
                .andExpect(jsonPath("$.data.bookmark").value(true))
                .andDo(print());

        entityManager.clear();
        assertThat(jdRepository.findById(jdId).get().isBookmark()).isTrue();

        // When: 즐겨찾기 해제 (false)
        BookmarkRequestDto unsetBookmark = new BookmarkRequestDto(false);
        ResultActions resultOff = mockMvc.perform(patch("/jds/{jd_id}/bookmark", jdId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(unsetBookmark)));

        // Then: 즐겨찾기 해제 확인
        resultOff.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("즐겨찾기 설정 완료"))
                .andExpect(jsonPath("$.data.jd_id").value(jdId))
                .andExpect(jsonPath("$.data.bookmark").value(false))
                .andDo(print());

        entityManager.clear();
        assertThat(jdRepository.findById(jdId).get().isBookmark()).isFalse();
    }

    @Test
    @DisplayName("JD 지원 완료 상태 토글 성공")
    @WithMockCustomUser(username = testUserLoginId, realName = testUserRealName, email = testUserEmail, role = "USER")
    void toggleApplyStatus_success() throws Exception {
        // Given
        JD jd = createAndSaveJd(
                setupMember,
                "지원 상태 테스트",
                "http://apply.com/jd/1",
                "테스트용 회사 이름",
                "테스트용 채용 공고 내용",
                "테스트용 직무",
                LocalDateTime.now(),
                "",
                false,
                false,
                null);
        Long jdId = jd.getId();

        // When: 지원 완료 상태로 변경 (applyAt이 null -> 현재 시간으로 설정)
        ResultActions resultApply = mockMvc.perform(patch("/jds/{jd_id}/apply", jdId));

        // Then: 지원 완료 확인
        resultApply.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("지원 완료 성공"))
                .andExpect(jsonPath("$.data.jd_id").value(jdId))
                .andExpect(jsonPath("$.data.applyAt").exists())
                .andDo(print());

        entityManager.clear();
        assertThat(jdRepository.findById(jdId).get().getApplyAt()).isNotNull();

        // When: 지원 완료 상태 취소 (applyAt이 현재 시간 -> null로 설정)
        ResultActions resultCancel = mockMvc.perform(patch("/jds/{jd_id}/apply", jdId));

        // Then: 지원 완료 취소 확인
        resultCancel.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("지원 완료 취소 성공"))
                .andExpect(jsonPath("$.data.jd_id").value(jdId))
                .andExpect(jsonPath("$.data.applyAt").isEmpty()) // applyAt이 null이 되었는지 확인
                .andDo(print());

        entityManager.clear();
        assertThat(jdRepository.findById(jdId).get().getApplyAt()).isNull();
    }

    @Test
    @DisplayName("JD 메모 업데이트 성공")
    @WithMockCustomUser(username = testUserLoginId, realName = testUserRealName, email = testUserEmail, role = "USER")
    void updateMemo_success() throws Exception {
        // Given
        JD jd = createAndSaveJd(
                setupMember,
                "메모 테스트",
                "http://memo.com/jd/1",
                "테스트용 회사 이름",
                "테스트용 채용 공고 내용",
                "테스트용 직무",
                LocalDateTime.now(),
                "초기 메모",
                false,
                false,
                null);
        Long jdId = jd.getId();

        // When
        MemoRequestDto updateMemoRequest = new MemoRequestDto("수정된 새로운 메모 내용입니다.");
        ResultActions result = mockMvc.perform(patch("/jds/{jd_id}/memo", jdId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateMemoRequest)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("메모 수정 성공"))
                .andExpect(jsonPath("$.data.jd_id").value(jdId))
                .andExpect(jsonPath("$.data.memo").value("수정된 새로운 메모 내용입니다."))
                .andDo(print());

        entityManager.clear();
        assertThat(jdRepository.findById(jdId).get().getMemo()).isEqualTo("수정된 새로운 메모 내용입니다.");
    }
}