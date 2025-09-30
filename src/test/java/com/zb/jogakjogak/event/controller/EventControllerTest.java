package com.zb.jogakjogak.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.zb.jogakjogak.event.entity.Event;
import com.zb.jogakjogak.event.repository.EventRepository;
import com.zb.jogakjogak.event.type.EventType;
import com.zb.jogakjogak.global.exception.EventErrorCode;
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
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class EventControllerTest {
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
    private EventRepository eventRepository;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private LLMService llmService;


    private final Faker faker = new Faker();

    private final String testUserLoginId = "testUserLoginId";
    private final String testUserRealName = "Test Real User";
    private final String testUserEmail = "test@email.com";

    private Member setupMember;
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
    @org.springframework.transaction.annotation.Transactional
    void tearDown() {
        toDoListRepository.deleteAllInBatch();
        jdRepository.deleteAllInBatch();
        resumeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        entityManager.clear();
        SecurityContextHolder.clearContext();
    }

    private void createAndSaveJdAndEvent(Member member,
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
                                         boolean isEvent) {
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

        if (isEvent) {
            eventRepository.save(Event.builder()
                    .code("TEST10")
                    .member(setupMember)
                    .type(EventType.NEW_MEMBER)
                    .isFirst(true)
                    .build());
        }

        entityManager.clear();
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

    @Test
    @DisplayName("새 사용자 이벤트 조회 성공")
    void getNewMemberEvent_success() throws Exception {
        //given
        createAndSaveJdAndEvent(
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
                null,
                true
        );

        //when
        ResultActions result = mockMvc.perform(get("/event"));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("새 이용자 이벤트 조회 성공"))
                .andExpect(jsonPath("$.data.code").value("TEST10"))
                .andExpect(jsonPath("$.data.type").value("NEW_MEMBER"))
                .andExpect(jsonPath("$.data.isFirst").value("true"))
                .andDo(print());

        //두번 째 요청에는 isFirst가 false로 변경됨
        //when
        ResultActions resultFalse = mockMvc.perform(get("/event"));

        //then
        resultFalse.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("새 이용자 이벤트 조회 성공"))
                .andExpect(jsonPath("$.data.code").value("TEST10"))
                .andExpect(jsonPath("$.data.type").value("NEW_MEMBER"))
                .andExpect(jsonPath("$.data.isFirst").value("false"))
                .andDo(print());
    }

    @Test
    @DisplayName("새 사용자 이벤트 조회 실패 - 이벤트가 없는 경우(이전 사용자)")
    void getNewMemberEvent_failure() throws Exception {
        //given
        createAndSaveJdAndEvent(
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
                null,
                false
        );

        //when
        ResultActions result = mockMvc.perform(get("/event"));

        //then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND_EVENT_CODE"))
                .andExpect(jsonPath("$.message").value(EventErrorCode.NOT_FOUND_EVENT_CODE.getMessage()))
                .andDo(print());
    }
}