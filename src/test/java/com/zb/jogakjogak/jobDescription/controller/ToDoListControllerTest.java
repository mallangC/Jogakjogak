package com.zb.jogakjogak.jobDescription.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.javafaker.Faker;
import com.zb.jogakjogak.jobDescription.domain.requestDto.BulkToDoListUpdateRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.CreateToDoListRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.ToDoListUpdateRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.UpdateToDoListRequestDto;
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
import org.mockito.MockitoAnnotations;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
class ToDoListControllerTest {

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
        objectMapper.registerModule(new JavaTimeModule());
        MockitoAnnotations.openMocks(this);

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
     * JD를 DB에 직접 저장하고 ToDoList를 함께 생성하는 유틸리티 메서드 (테스트 데이터 세팅용)
     * ToDoListControllerTest에 맞게 ToDoList 생성 로직 강화
     *
     * @param member       JD를 소유할 멤버
     * @param title        JD 제목
     * @param url          JD URL
     * @param dueDate      마감일
     * @param memo         메모 내용
     * @param isBookmarked 즐겨찾기 여부
     * @param isAlarmOn    알림 설정 여부
     * @param applyAt      지원 완료일 (null이면 미완료)
     * @param toDoLists    생성할 ToDoList 목록 (선택 사항)
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
                               LocalDateTime applyAt,
                               List<ToDoList> toDoLists) {
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

        if (toDoLists != null && !toDoLists.isEmpty()) {
            toDoLists.forEach(toDoList -> toDoList.setJd(savedJd)); // JD 설정
            toDoListRepository.saveAll(toDoLists); // ToDoList 저장
        } else {
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
        }
        entityManager.clear();
        return savedJd;
    }

    /**
     * 특정 JD에 새로운 ToDoList를 생성합니다.
     */
    @Test
    @DisplayName("ToDoList 생성 성공")
    @WithMockCustomUser(username = testUserLoginId, realName = testUserRealName, email = testUserEmail)
    void createToDoList_success() throws Exception {
        // Given
        JD jd = createAndSaveJd(
                setupMember,
                "테스트 JD",
                "https://test.com",
                "회사",
                "내용",
                "직무",
                LocalDateTime.now(),
                "",
                false,
                false,
                null,
                null);
        Long jdId = jd.getId();

        CreateToDoListRequestDto dto = CreateToDoListRequestDto.builder()
                .title("새로운 투두리스트 제목")
                .content("새로운 투두리스트 내용")
                .category(ToDoListType.STRUCTURAL_COMPLEMENT_PLAN)
                .build();
        String content = objectMapper.writeValueAsString(dto);

        // When
        ResultActions result = mockMvc.perform(post("/api/jds/{jdId}/to-do-lists", jdId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));

        // Then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("체크리스트 추가 완료"))
                .andExpect(jsonPath("$.data.checklist_id").exists())
                .andExpect(jsonPath("$.data.title").value("새로운 투두리스트 제목"))
                .andExpect(jsonPath("$.data.content").value("새로운 투두리스트 내용"))
                .andExpect(jsonPath("$.data.memo").value(""))
                .andExpect(jsonPath("$.data.done").value(false))
                .andExpect(jsonPath("$.data.category").value(ToDoListType.STRUCTURAL_COMPLEMENT_PLAN.name()))
                .andDo(print());

        // DB에 저장되었는지 확인
        entityManager.clear();
        List<ToDoList> toDoLists = toDoListRepository.findAllByJdId(jdId);
        assertThat(toDoLists).hasSize(4); // 기존 3개 + 새로 추가된 1개
        assertThat(toDoLists).extracting(ToDoList::getTitle).contains("새로운 투두리스트 제목");
    }

    /**
     * 특정 JD에 속한 기존 ToDoList의 내용을 수정합니다.
     */
    @Test
    @DisplayName("ToDoList 수정 성공")
    @WithMockCustomUser(username = testUserLoginId, realName = testUserRealName, email = testUserEmail)
    void updateToDoList_success() throws Exception {
        // Given
        JD jd = createAndSaveJd(setupMember,
                "테스트 JD",
                "https://test.com",
                "회사",
                "내용",
                "직무",
                LocalDateTime.now(),
                "",
                false,
                false,
                null,
                null
        );
        Long jdId = jd.getId();
        ToDoList existingToDoList = toDoListRepository.findAllByJdId(jdId).get(0);
        Long toDoListId = existingToDoList.getId();

        UpdateToDoListRequestDto dto = new UpdateToDoListRequestDto(
               ToDoListType.SCHEDULE_MISC_ERROR,
                "수정된 제목",
                "수정된 내용",
                true
        );
        String content = objectMapper.writeValueAsString(dto);

        // When
        ResultActions result = mockMvc.perform(patch("/api/jds/{jdId}/to-do-lists/{toDoListId}", jdId, toDoListId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("체크리스트 수정 완료"))
                .andExpect(jsonPath("$.data.checklist_id").value(toDoListId))
                .andExpect(jsonPath("$.data.title").value("수정된 제목"))
                .andExpect(jsonPath("$.data.content").value("수정된 내용"))
                .andExpect(jsonPath("$.data.memo").value(""))
                .andExpect(jsonPath("$.data.done").value(true))
                .andDo(print());

        // DB에서 수정되었는지 확인
        entityManager.clear();
        ToDoList updatedToDoList = toDoListRepository.findById(toDoListId).orElseThrow();
        assertThat(updatedToDoList.getTitle()).isEqualTo("수정된 제목");
        assertThat(updatedToDoList.getContent()).isEqualTo("수정된 내용");
        assertThat(updatedToDoList.getMemo()).isEqualTo("");
    }

    /**
     * 특정 JD에 속한 단일 ToDoList의 상세 정보를 조회합니다.
     */
    @Test
    @DisplayName("ToDoList 단건 조회 성공")
    @WithMockCustomUser(username = testUserLoginId, realName = testUserRealName, email = testUserEmail)
    void getToDoList_success() throws Exception {
        // Given
        JD jd = createAndSaveJd(
                setupMember,
                "조회용 JD",
                "https://get.com",
                "회사",
                "내용",
                "직무",
                LocalDateTime.now(),
                "",
                false,
                false,
                null,
                null
        );
        Long jdId = jd.getId();
        ToDoList existingToDoList = toDoListRepository.findAllByJdId(jdId).get(1); // 두 번째 ToDoList 조회
        Long toDoListId = existingToDoList.getId();

        // When
        ResultActions result = mockMvc.perform(get("/api/jds/{jdId}/to-do-lists/{toDoListId}", jdId, toDoListId));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("체크리스트 조회 성공"))
                .andExpect(jsonPath("$.data.checklist_id").value(toDoListId))
                .andExpect(jsonPath("$.data.title").value(existingToDoList.getTitle()))
                .andExpect(jsonPath("$.data.content").value(existingToDoList.getContent()))
                .andDo(print());
    }

    /**
     * 특정 JD에 속한 단일 ToDoList를 삭제합니다.
     */
    @Test
    @DisplayName("ToDoList 삭제 성공")
    @WithMockCustomUser(username = testUserLoginId, realName = testUserRealName, email = testUserEmail)
    void deleteToDoList_success() throws Exception {
        // Given
        JD jd = createAndSaveJd(
                setupMember,
                "삭제용 JD",
                "https://delete.com",
                "회사",
                "내용",
                "직무",
                LocalDateTime.now(),
                "",
                false,
                false,
                null,
                null
        );
        Long jdId = jd.getId();
        ToDoList toDoListToDelete = toDoListRepository.findAllByJdId(jdId).get(0);
        Long toDoListId = toDoListToDelete.getId();

        // When
        ResultActions result = mockMvc.perform(delete("/api/jds/{jdId}/to-do-lists/{toDoListId}", jdId, toDoListId));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("체크리스트 삭제 성공"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());

        // DB에서 삭제되었는지 확인
        entityManager.clear();
        assertThat(toDoListRepository.findById(toDoListId)).isEmpty();
        assertThat(toDoListRepository.findAllByJdId(jdId)).hasSize(2);
    }

    /**
     * 특정 JD에 속한 특정 카테고리의 모든 ToDoList들을 조회합니다.
     */
    @Test
    @DisplayName("ToDoList 카테고리별 조회 성공")
    @WithMockCustomUser(username = testUserLoginId, realName = testUserRealName, email = testUserEmail)
    void getToDoListsByCategory_success() throws Exception {
        // Given
        JD jd = createAndSaveJd(
                setupMember,
                "카테고리 조회용 JD",
                "https://category.com",
                "회사",
                "내용",
                "직무",
                LocalDateTime.now(),
                "",
                false,
                false,
                null,
                null);
        Long jdId = jd.getId();

        toDoListRepository.save(ToDoList.builder()
                .title("추가 투두 1")
                .content("추가 내용 1")
                .category(ToDoListType.CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL)
                .isDone(false)
                .jd(jd)
                .build());
        toDoListRepository.save(ToDoList.builder()
                .title("추가 투두 2")
                .content("추가 내용 2")
                .category(ToDoListType.CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL)
                .isDone(true)
                .jd(jd)
                .build());
        entityManager.clear();

        // When
        ResultActions result = mockMvc.perform(get("/api/jds/{jdId}/to-do-lists", jdId)
                .param("category", ToDoListType.STRUCTURAL_COMPLEMENT_PLAN.name()));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("카테고리별 투두리스트 조회 성공"))
                .andExpect(jsonPath("$.data.category").value(ToDoListType.STRUCTURAL_COMPLEMENT_PLAN.name()))
                .andExpect(jsonPath("$.data.toDoLists").isArray())
                .andExpect(jsonPath("$.data.toDoLists.length()").value(3))
                .andDo(print());

        // When
        ResultActions result2 = mockMvc.perform(get("/api/jds/{jdId}/to-do-lists", jdId)
                .param("category", ToDoListType.CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL.name()));

        // Then
        result2.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("카테고리별 투두리스트 조회 성공"))
                .andExpect(jsonPath("$.data.category").value(ToDoListType.CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL.name()))
                .andExpect(jsonPath("$.data.toDoLists").isArray())
                .andExpect(jsonPath("$.data.toDoLists.length()").value(2))
                .andDo(print());
    }

    /**
     * 특정 JD에 속한 여러 ToDoList를 일괄적으로 생성, 수정, 삭제합니다.
     */
    @Test
    @DisplayName("ToDoList 일괄 업데이트 성공 - 생성, 수정, 삭제")
    @WithMockCustomUser(username = testUserLoginId, realName = testUserRealName, email = testUserEmail)
    void bulkUpdateToDoLists_success_createUpdateDelete() throws Exception {
        // Given
        JD jd = createAndSaveJd(
                setupMember,
                "일괄 업데이트 JD",
                "https://bulk.com",
                "회사",
                "내용",
                "직무",
                LocalDateTime.now(),
                "",
                false,
                false,
                null,
                null
        );
        Long jdId = jd.getId();
        entityManager.clear();

        List<ToDoList> existingToDoLists = toDoListRepository.findToDoListsByJdIdAndCategoryWithJd(jdId, ToDoListType.STRUCTURAL_COMPLEMENT_PLAN);
        Long toDoListIdToUpdate = existingToDoLists.get(0).getId();
        Long toDoListIdToDelete = existingToDoLists.get(1).getId();

        ToDoListUpdateRequestDto newToDoList1 = ToDoListUpdateRequestDto.builder()
                .title("새로 생성할 투두1")
                .content("새로운 내용1")
                .isDone(false)
                .build();
        ToDoListUpdateRequestDto newToDoList2 = ToDoListUpdateRequestDto.builder()
                .title("새로 생성할 투두2")
                .content("새로운 내용2")
                .isDone(false)
                .build();

        ToDoListUpdateRequestDto updateToDoList = ToDoListUpdateRequestDto.builder()
                .title("수정된 기존 투두 제목")
                .content("수정된 기존 투두 내용")
                .isDone(true)
                .id(toDoListIdToUpdate)
                .build();
        // Bulk 요청 DTO 생성
        BulkToDoListUpdateRequestDto bulkDto = new BulkToDoListUpdateRequestDto(
                ToDoListType.STRUCTURAL_COMPLEMENT_PLAN,
                Arrays.asList(newToDoList1, newToDoList2, updateToDoList),
                List.of(toDoListIdToDelete)
        );
        String content = objectMapper.writeValueAsString(bulkDto);

        // When
        ResultActions result = mockMvc.perform(put("/api/jds/{jdId}/to-do-lists/bulk-update", jdId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("다중 투두리스트 수정 성공"))
                .andExpect(jsonPath("$.data.category").value(ToDoListType.STRUCTURAL_COMPLEMENT_PLAN.name()))
                .andExpect(jsonPath("$.data.toDoLists").isArray())
                .andExpect(jsonPath("$.data.toDoLists.length()").value(4))
                .andDo(print());

        // DB에서 최종 상태 확인
        entityManager.clear();
        List<ToDoList> finalToDoLists = toDoListRepository.findToDoListsByJdIdAndCategoryWithJd(jdId, ToDoListType.STRUCTURAL_COMPLEMENT_PLAN);
        assertThat(finalToDoLists).hasSize(4);

        // 생성된 항목 확인
        assertThat(finalToDoLists).extracting(ToDoList::getTitle).contains("새로 생성할 투두1", "새로 생성할 투두2");
        assertThat(finalToDoLists).extracting(ToDoList::isDone).contains(true, false);

        // 수정된 항목 확인
        ToDoList actualUpdated = finalToDoLists.stream()
                .filter(tl -> tl.getId().equals(toDoListIdToUpdate))
                .findFirst()
                .orElseThrow();
        assertThat(actualUpdated.getTitle()).isEqualTo("수정된 기존 투두 제목");
        assertThat(actualUpdated.getContent()).isEqualTo("수정된 기존 투두 내용");
        assertThat(actualUpdated.getMemo()).isEqualTo("");
        assertThat(actualUpdated.isDone()).isTrue();

        // 삭제된 항목 확인
        assertThat(toDoListRepository.findById(toDoListIdToDelete)).isEmpty();
    }

    @Test
    @DisplayName("ToDoList 일괄 업데이트 성공 - 빈 요청으로 변경 없음")
    @WithMockCustomUser(username = testUserLoginId, realName = testUserRealName, email = testUserEmail)
    void bulkUpdateToDoLists_success_emptyRequest() throws Exception {
        // Given
        JD jd = createAndSaveJd(setupMember, "빈 요청 JD", "http://empty.com", "회사", "내용", "직무", LocalDateTime.now(), "", false, false, null, null);
        Long jdId = jd.getId();
        entityManager.clear();

        // 초기 ToDoList 개수 확인
        List<ToDoList> initialToDoLists = toDoListRepository.findToDoListsByJdIdAndCategoryWithJd(jdId, ToDoListType.STRUCTURAL_COMPLEMENT_PLAN);
        int initialSize = initialToDoLists.size();

        // 빈 Bulk 요청 DTO 생성 (아무런 생성, 수정, 삭제 요청 없음)
        BulkToDoListUpdateRequestDto bulkDto = new BulkToDoListUpdateRequestDto(
                ToDoListType.STRUCTURAL_COMPLEMENT_PLAN,
                List.of(),
                List.of()
        );
        String content = objectMapper.writeValueAsString(bulkDto);

        // When
        ResultActions result = mockMvc.perform(put("/api/jds/{jdId}/to-do-lists/bulk-update", jdId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("다중 투두리스트 수정 성공"))
                .andExpect(jsonPath("$.data.category").value(ToDoListType.STRUCTURAL_COMPLEMENT_PLAN.name()))
                .andExpect(jsonPath("$.data.toDoLists").isArray())
                .andExpect(jsonPath("$.data.toDoLists.length()").value(initialSize))
                .andDo(print());

        // DB에서 변경 없음 확인
        entityManager.clear();
        List<ToDoList> finalToDoLists = toDoListRepository.findToDoListsByJdIdAndCategoryWithJd(jdId, ToDoListType.STRUCTURAL_COMPLEMENT_PLAN);

        // 두 리스트의 ID를 추출하여 비교합니다.
        List<Long> initialToDoListIds = initialToDoLists.stream().map(ToDoList::getId).collect(Collectors.toList());
        List<Long> finalToDoListIds = finalToDoLists.stream().map(ToDoList::getId).collect(Collectors.toList());

        assertThat(finalToDoListIds).containsExactlyInAnyOrderElementsOf(initialToDoListIds);
    }
}