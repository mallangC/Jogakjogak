package com.zb.jogakjogak.jobDescription.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.javafaker.Faker;
import com.zb.jogakjogak.jobDescription.domain.requestDto.*;
import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.jobDescription.entity.ToDoList;
import com.zb.jogakjogak.jobDescription.repository.JDRepository;
import com.zb.jogakjogak.jobDescription.repository.ToDoListRepository;
import com.zb.jogakjogak.jobDescription.service.LLMService;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import com.zb.jogakjogak.resume.entity.Resume;
import com.zb.jogakjogak.resume.repository.ResumeRepository;
import com.zb.jogakjogak.security.Role;
import com.zb.jogakjogak.security.dto.CustomOAuth2User;
import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Commit;
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
@AutoConfigureMockMvc(addFilters = false)
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
        setAuthenticationForTestUser(testUserLoginId);
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

    private void setAuthenticationForTestUser(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new AssertionError("테스트 사용자를 찾을 수 없습니다."));
        CustomOAuth2User principal = new CustomOAuth2User(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 특정 JD에 새로운 ToDoList를 생성합니다.
     */
    @Test
    @DisplayName("ToDoList 생성 성공")
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
        ResultActions result = mockMvc.perform(post("/jds/{jdId}/to-do-lists", jdId)
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
        ResultActions result = mockMvc.perform(patch("/jds/{jdId}/to-do-lists/{toDoListId}", jdId, toDoListId)
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

    }

    /**
     * 특정 JD에 속한 기존 ToDoList의 완료 여부를 수정합니다.
     */
    @Test
    @DisplayName("ToDoList 수정 성공")
    void toggleComplete_success() throws Exception {
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

        ToggleTodolistRequestDto dto = new ToggleTodolistRequestDto(true);
        String content = objectMapper.writeValueAsString(dto);

        // When
        ResultActions result = mockMvc.perform(patch("/jds/{jdId}/to-do-lists/{toDoListId}/isDone", jdId, toDoListId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("체크리스트 완료 여부 수정 완료"))
                .andExpect(jsonPath("$.data.checklist_id").value(toDoListId))
                .andExpect(jsonPath("$.data.done").value(true))
                .andDo(print());

    }

    /**
     * 특정 JD에 속한 단일 ToDoList의 상세 정보를 조회합니다.
     */
    @Test
    @DisplayName("ToDoList 단건 조회 성공")
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
        ResultActions result = mockMvc.perform(get("/jds/{jdId}/to-do-lists/{toDoListId}", jdId, toDoListId));

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
        ResultActions result = mockMvc.perform(delete("/jds/{jdId}/to-do-lists/{toDoListId}", jdId, toDoListId));

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
        ResultActions result = mockMvc.perform(get("/jds/{jdId}/to-do-lists", jdId)
                .param("category", ToDoListType.STRUCTURAL_COMPLEMENT_PLAN.name()));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("카테고리별 투두리스트 조회 성공"))
                .andExpect(jsonPath("$.data.category").value(ToDoListType.STRUCTURAL_COMPLEMENT_PLAN.name()))
                .andExpect(jsonPath("$.data.toDoLists").isArray())
                .andExpect(jsonPath("$.data.toDoLists.length()").value(3))
                .andDo(print());

        // When
        ResultActions result2 = mockMvc.perform(get("/jds/{jdId}/to-do-lists", jdId)
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
        ResultActions result = mockMvc.perform(put("/jds/{jdId}/to-do-lists/bulk-update", jdId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("다중 투두리스트 수정 성공"))
                .andExpect(jsonPath("$.data.category").value(ToDoListType.STRUCTURAL_COMPLEMENT_PLAN.name()))
                .andExpect(jsonPath("$.data.toDoLists").isArray())
                .andExpect(jsonPath("$.data.toDoLists.length()").value(4))
                .andDo(print());

    }

    @Test
    @DisplayName("ToDoList 일괄 업데이트 성공 - 빈 요청으로 변경 없음")
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
        ResultActions result = mockMvc.perform(put("/jds/{jdId}/to-do-lists/bulk-update", jdId)
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

    @Test
    @DisplayName("ToDoList 완료여부 일괄 수정 성공 - 여러 투두리스트를 완료 처리")
    void updateIsDoneTodoLists_success_markAsDone() throws Exception {
        // Given
        JD jd = createAndSaveJd(
                setupMember,
                "완료여부 일괄 수정 JD",
                "https://bulk-done.com",
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

        // 추가 ToDoList 생성 (다양한 카테고리)
        ToDoList additionalTodo1 = ToDoList.builder()
                .title("추가 투두 1")
                .content("추가 내용 1")
                .category(ToDoListType.CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL)
                .isDone(false)
                .jd(jd)
                .build();
        ToDoList additionalTodo2 = ToDoList.builder()
                .title("추가 투두 2")
                .content("추가 내용 2")
                .category(ToDoListType.SCHEDULE_MISC_ERROR)
                .isDone(false)
                .jd(jd)
                .build();
        toDoListRepository.save(additionalTodo1);
        toDoListRepository.save(additionalTodo2);

        // 수정할 ToDoList ID 목록 가져오기
        List<ToDoList> existingToDoLists = toDoListRepository.findAllByJdId(jdId);
        List<Long> toDoListIdsToUpdate = existingToDoLists.stream()
                .limit(3)
                .map(ToDoList::getId)
                .collect(Collectors.toList());

        System.out.println("=== 수정 전 ToDoList 상태 ===");
        existingToDoLists.stream()
                .limit(3)
                .forEach(todo -> System.out.println("ID: " + todo.getId() + ", isDone: " + todo.isDone()));

        UpdateTodoListsIsDoneRequestDto dto = UpdateTodoListsIsDoneRequestDto.builder()
                .toDoListIds(toDoListIdsToUpdate)
                .isDone(true)
                .build();
        String content = objectMapper.writeValueAsString(dto);

        // When
        ResultActions result = mockMvc.perform(put("/jds/{jdId}/to-do-lists/update-is-done", jdId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("다중 투두리스트 완료여부 수정 성공"))
                .andExpect(jsonPath("$.data.toDoLists").isArray())
                .andDo(print());

        // 트랜잭션 강제 커밋 및 캐시 클리어
        entityManager.flush();
        entityManager.clear();

        // DB에서 실제로 수정되었는지 확인
        List<ToDoList> updatedToDoLists = toDoListRepository.findAllById(toDoListIdsToUpdate);

        System.out.println("=== 수정 후 ToDoList 상태 ===");
        updatedToDoLists.forEach(todo ->
                System.out.println("ID: " + todo.getId() + ", isDone: " + todo.isDone()));

        assertThat(updatedToDoLists).hasSize(3);
        updatedToDoLists.forEach(todo -> {
            assertThat(todo.isDone())
                    .as("ToDoList ID %d의 isDone이 true", todo.getId())
                    .isTrue();
        });
    }

}