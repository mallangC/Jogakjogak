package com.zb.jogakjogak.resume.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.zb.jogakjogak.resume.entity.SkillWord;
import com.zb.jogakjogak.resume.repository.SkillWordRepository;
import com.zb.jogakjogak.security.Role;
import com.zb.jogakjogak.security.dto.CustomOAuth2User;
import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class SkillWordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private SkillWordRepository skillWordRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Faker faker = new Faker();

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .name("testUserName")
                .phoneNumber("01000000000")
                .username("testUserLoginId")
                .password(faker.internet().password())
                .email("test@email.com")
                .nickname(faker.animal().name())
                .role(Role.USER)
                .lastLoginAt(LocalDateTime.now())
                .build();
        memberRepository.save(testMember);
        setSkillWords();
        setAuthenticationForTestUser("testUserLoginId");
    }

    private void setSkillWords() {
        List<SkillWord> skills = Arrays.asList(
                SkillWord.builder().content("java").build(),
                SkillWord.builder().content("spring").build(),
                SkillWord.builder().content("javaScript").build(),
                SkillWord.builder().content("JPA").build(),
                SkillWord.builder().content("PyThon").build(),
                SkillWord.builder().content("SpringBoot").build(),
                SkillWord.builder().content("자바").build(),
                SkillWord.builder().content("Node.js").build(),
                SkillWord.builder().content("스프링").build(),
                SkillWord.builder().content("스프링부트").build(),
                SkillWord.builder().content("Mysql").build()
        );
        skillWordRepository.saveAll(skills);
    }

    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
        skillWordRepository.deleteAllInBatch();
        entityManager.clear();
        SecurityContextHolder.clearContext();
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
    @DisplayName("스킬 검색 기능 성공 - 영어단어")
    void autoComplete_success_withEnglishWord() throws Exception {
        //Given
        String query = "java";

        //When
        ResultActions result = mockMvc.perform(get("/resume/skill-word")
                .param("q", query)
                .contentType(MediaType.APPLICATION_JSON));

        //Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("스킬 단어 검색 완료"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data.[0]").value("java"))
                .andExpect(jsonPath("$.data.[1]").value("javaScript"))
                .andDo(print());
    }

    @Test
    @DisplayName("스킬 검색 기능 성공 - 한국어")
    void autoComplete_success_withKoreaWord() throws Exception {
        //Given
        String query = "스프링";

        //When
        ResultActions result = mockMvc.perform(get("/resume/skill-word")
                .param("q", query)
                .contentType(MediaType.APPLICATION_JSON));

        //Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("스킬 단어 검색 완료"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data.[0]").value("스프링"))
                .andExpect(jsonPath("$.data.[1]").value("스프링부트"))
                .andDo(print());
    }

}