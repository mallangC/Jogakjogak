package com.zb.jogakjogak.resume.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.zb.jogakjogak.global.HttpApiResponse;
import com.zb.jogakjogak.resume.domain.requestDto.ResumeRequestDto;
import com.zb.jogakjogak.resume.domain.responseDto.ResumeResponseDto;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResumeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private EntityManager entityManager;

    private Faker faker = new Faker();

    private final String testUserLoginId = "testUserLoginId";
    private final String testUserRealName = "Test Real User";
    private final String testUserEmail = "test@email.com";

    private final String otherUserLoginId = "otherUser";
    private final String otherUserRealName = "Other User";
    private final String otherUserEmail = "other@example.com";

    private Member setupMember;
    private Member otherMember;

    @BeforeEach
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

    }

    @AfterEach
    void tearDown() {
        resumeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        entityManager.clear();
        SecurityContextHolder.clearContext();
    }

    /**
     * 이력서 등록 테스트 유틸리티 (다른 테스트에서 재사용)
     * 이 메서드는 MockMvc를 통해 이력서를 등록하고, 등록된 이력서의 ID를 반환합니다.
     * @param title 이력서 제목
     * @return 등록된 이력서의 ID
     */
    private Long registerResumeThroughApi(String title) throws Exception {
        String generatedContent = generateLongContent(300);
        ResumeRequestDto requestDto = new ResumeRequestDto(title, generatedContent);
        String requestContent = objectMapper.writeValueAsString(requestDto);

        ResultActions registerResult = mockMvc.perform(post("/resume")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestContent)
        );

        HttpApiResponse<ResumeResponseDto> response = objectMapper.readValue(registerResult.andReturn().getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        registerResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이력서 등록 완료"));

        entityManager.clear();
        Member updatedMember = memberRepository.findByUsername(testUserLoginId).orElseThrow();
        assertThat(updatedMember.getResume()).isNotNull();

        return response.data().getResumeId();
    }


    @Test
    @DisplayName("이력서 등록 성공")
    @WithMockCustomUser(username = testUserLoginId, realName = testUserRealName, email = testUserEmail)
    void registerResume_success() throws Exception {
        // Given
        String testTitle = "테스트 이력서 제목";
        String testContent = generateLongContent(300);

        ResumeRequestDto requestDto = new ResumeRequestDto(testTitle, testContent);
        String content = objectMapper.writeValueAsString(requestDto);

        // When
        ResultActions result = mockMvc.perform(post("/resume")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이력서 등록 완료"))
                .andExpect(jsonPath("$.data.title").value(testTitle))
                .andExpect(jsonPath("$.data.content").value(testContent))
                .andExpect(jsonPath("$.data.resumeId").isNumber())
                .andDo(print());

        entityManager.clear();
        Member memberAfterRegister = memberRepository.findByUsername(testUserLoginId).orElseThrow();
        assertThat(memberAfterRegister.getResume()).isNotNull();
        assertThat(resumeRepository.findById(memberAfterRegister.getResume().getId())).isPresent();
    }

    @Test
    @DisplayName("이력서 수정 성공")
    @WithMockCustomUser(username = testUserLoginId, realName = testUserRealName, email = testUserEmail)
    void modifyResume_success() throws Exception {
        // Given
        Long resumeId = registerResumeThroughApi("원본 이력서");

        String updatedTitle = "수정된 이력서 제목";
        String updatedContent = generateLongContent(300);

        ResumeRequestDto updateRequestDto = new ResumeRequestDto(updatedTitle, updatedContent);
        String content = objectMapper.writeValueAsString(updateRequestDto);

        // When
        ResultActions result = mockMvc.perform(patch("/resume/{resume_id}", resumeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));
        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이력서 수정 완료"))
                .andExpect(jsonPath("$.data.title").value(updatedTitle))
                .andExpect(jsonPath("$.data.content").value(updatedContent))
                .andDo(print());


        entityManager.clear();
        Optional<Resume> updatedResume = resumeRepository.findById(resumeId);
        assertThat(updatedResume).isPresent();
        assertThat(updatedResume.get().getTitle()).isEqualTo(updatedTitle);
        assertThat(updatedResume.get().getContent()).isEqualTo(updatedContent);
    }

    @Test
    @DisplayName("이력서 조회 성공")
    @WithMockCustomUser(username = testUserLoginId, realName = testUserRealName, email = testUserEmail)
    void getResume_success() throws Exception {
        //Given
        Long resumeId = registerResumeThroughApi("조회할 이력서 제목");

        //When
        ResultActions result = mockMvc.perform(get("/resume/{resumeId}", resumeId));

        //Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이력서 조회 성공"))
                .andExpect(jsonPath("$.data.title").value("조회할 이력서 제목"))
                .andExpect(jsonPath("$.data.content").isNotEmpty())
                .andDo(print());
    }

    @Test
    @DisplayName("이력서 삭제 성공")
    @WithMockCustomUser(username = testUserLoginId, realName = testUserRealName, email = testUserEmail)
    @Transactional
    @Commit
    void deleteResume_success() throws Exception {
        //Given
        Long resumeIdToDelete = registerResumeThroughApi("삭제할 이력서 제목"); // 300자 이상 내용 자동 생성

        //When
        ResultActions result = mockMvc.perform(delete("/resume/{resumeId}", resumeIdToDelete));

        //Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이력서 삭제 성공"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());

        entityManager.clear();

        Member updatedMember = memberRepository.findByUsername(testUserLoginId)
                .orElseThrow(() -> new AssertionError("삭제 후에도 멤버를 찾을 수 없습니다."));
        assertThat(updatedMember.getResume()).isNull();

        Optional<Resume> deletedResume = resumeRepository.findResumeWithMemberByIdAndMemberId(resumeIdToDelete, setupMember.getId());
        assertThat(deletedResume).isEmpty();

        mockMvc.perform(get("/resume/{resumeId}", resumeIdToDelete))
                .andExpect(status().isForbidden())
                .andDo(print());
    }
    @Test
    @DisplayName("유효하지 않은 (무의미한) 이력서 내용 입력 시 유효성 검사 실패")
    @WithMockCustomUser(username = "testUserLoginId", realName = "Test User", email = "test@email.com")
    void registerResume_invalidContent_meaningless() throws Exception {
        // Given
        String testTitle = "유효한 테스트 제목";
        String meaninglessContent = faker.lorem().characters(350, true, false);

        ResumeRequestDto requestDto = new ResumeRequestDto(testTitle, meaninglessContent);
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // When
        ResultActions result = mockMvc.perform(post("/resume")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // Then
        result.andExpect(status().isBadRequest()) // 400 Bad Request
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("이력서 내용이 유효하지 않거나 의미 없는 반복 문자를 포함합니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("유효하지 않은 (한글 무작위) 이력서 내용 입력 시 유효성 검사 실패")
    @WithMockCustomUser(username = "testUserLoginId", realName = "Test User", email = "test@email.com")
    void registerResume_invalidContent_koreanGibberish() throws Exception {
        // Given
        String testTitle = "유효한 제목";
        StringBuilder koreanGibberishBuilder = new StringBuilder();
        String pattern = "ㅁㄴㅇㄹㅂㅈㄱㄷㅅ";
        while(koreanGibberishBuilder.length() < 300) {
            koreanGibberishBuilder.append(pattern);
        }
        String meaninglessKoreanContent = koreanGibberishBuilder.toString().substring(0, 300);

        ResumeRequestDto requestDto = new ResumeRequestDto(testTitle, meaninglessKoreanContent);
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // When
        ResultActions result = mockMvc.perform(post("/resume")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // Then
        result.andExpect(status().isBadRequest()) // 400 Bad Request
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("이력서 내용이 유효하지 않거나 의미 없는 반복 문자를 포함합니다."))
                .andDo(print());
    }

    /**
     * Faker를 사용하여 지정된 길이 이상의 랜덤 텍스트를 생성하는 헬퍼 메서드
     * @param minLength 최소 길이
     * @return 생성된 랜덤 텍스트
     */
    private String generateLongContent(int minLength) {
        StringBuilder contentBuilder = new StringBuilder();
        while (contentBuilder.length() < minLength) {
            contentBuilder.append(faker.lorem().paragraph(2));
            contentBuilder.append(" ");
        }
        return contentBuilder.toString();
    }

}
