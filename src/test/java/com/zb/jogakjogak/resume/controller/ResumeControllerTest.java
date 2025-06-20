package com.zb.jogakjogak.resume.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.zb.jogakjogak.global.HttpApiResponse;
import com.zb.jogakjogak.resume.domain.requestDto.ResumeRequestDto;
import com.zb.jogakjogak.resume.domain.responseDto.ResumeResponseDto;
import com.zb.jogakjogak.resume.repository.ResumeRepository;
import com.zb.jogakjogak.security.Role;
import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.jwt.JWTUtil;
import com.zb.jogakjogak.security.repository.MemberRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ResumeControllerTest {

    @MockitoBean
    private JWTUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Value("${jwt.secret-key}")
    private String secretKeyString;

    private Faker faker = new Faker();
    private Key testSigningKey;
    private final String username = faker.name().fullName();
    private String token;

    @BeforeEach
    void setUp() {
        this.testSigningKey = new SecretKeySpec(
                secretKeyString.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );

        Member testMember = Member.builder()
                .name(username)
                .phoneNumber("01000000000")
                .username(username)
                .password(faker.internet().password())
                .email(faker.internet().emailAddress())
                .nickname(faker.animal().name())
                .role(Role.USER)
                .lastLoginAt(LocalDateTime.now())
                .build();
        memberRepository.save(testMember);

        token = createTestJwtToken(username);
        setupJwtUtilMock();
    }


    private String createTestJwtToken(String username) {
        if (testSigningKey == null) {
            throw new IllegalStateException("Test signing key has not been initialized.");
        }

        return Jwts.builder()
                .subject(username)
                .claim("role", "USER")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(this.testSigningKey, SignatureAlgorithm.HS256)
                .compact(); // 토큰 압축 및 반환
    }

    private void setupJwtUtilMock() {
        Mockito.when(jwtUtil.isExpired(Mockito.anyString())).thenReturn(false);
        Mockito.when(jwtUtil.getToken(Mockito.anyString())).thenReturn("ACCESS_TOKEN");
        Mockito.when(jwtUtil.getUserName(Mockito.anyString())).thenReturn(username);
        Mockito.when(jwtUtil.getRole(Mockito.anyString())).thenReturn(Role.USER.toString());
    }



    @Test
    @DisplayName("이력서 등록 성공")
    void registerResume_success() throws Exception {
        // Given
        ResumeRequestDto requestDto = new ResumeRequestDto("테스트 이력서 제목", "테스트 이력서 내용입니다.");
        String content = objectMapper.writeValueAsString(requestDto);

        // When
        ResultActions result = mockMvc.perform(post("/api/resume")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(content));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이력서 등록 완료"))
                .andDo(print());
    }

    @Test
    @DisplayName("이력서 수정 성공")
    void modifyResume_success() throws Exception{
        // Given

        ResumeRequestDto initialRequestDto = new ResumeRequestDto("원본 이력서", "원본 내용");
        String initialContent = objectMapper.writeValueAsString(initialRequestDto);
        ResultActions registerResult = mockMvc.perform(post("/api/resume")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(initialContent));

        HttpApiResponse<ResumeResponseDto> response = objectMapper.readValue(registerResult.andReturn().getResponse().getContentAsString()
                , new TypeReference<>() {});
        Long resumeId = response.data().getResumeId();

        ResumeRequestDto updateRequestDto = new ResumeRequestDto("수정된 이력서 제목", "수정된 내용입니다.");
        String content = objectMapper.writeValueAsString(updateRequestDto);

        //When
        ResultActions result = mockMvc.perform(patch("/api/resume/{resume_id}", resumeId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));
        //Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이력서 수정 완료"))
                .andExpect(jsonPath("$.data.title").value("수정된 이력서 제목"))
                .andExpect(jsonPath("$.data.content").value("수정된 내용입니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("이력서 조회 성공")
    void getResume_success() throws Exception {
        //Given
        ResumeRequestDto initialRequestDto = new ResumeRequestDto("조회할 이력서 제목", "조회할 내용");
        String initialContent = objectMapper.writeValueAsString(initialRequestDto);
        ResultActions registerResult = mockMvc.perform(post("/api/resume")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(initialContent));
        HttpApiResponse<ResumeResponseDto> response = objectMapper.readValue(registerResult.andReturn().getResponse().getContentAsString()
                , new TypeReference<>() {});
        Long resumeId = response.data().getResumeId();

        //When
        ResultActions result = mockMvc.perform(get("/api/resume/{resumeId}", resumeId)
                .header("Authorization", "Bearer " + token)
        );

        //Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이력서 조회 성공"))
                .andExpect(jsonPath("$.data.title").value("조회할 이력서 제목"))
                .andExpect(jsonPath("$.data.content").value("조회할 내용"))
                .andDo(print());

    }

    @Test
    @DisplayName("이력서 삭제 성공")
    void deleteResume_success() throws Exception{
        //Given
        ResumeRequestDto initialRequestDto = new ResumeRequestDto("삭제할 이력서 제목", "삭제할 내용");
        String initialContent = objectMapper.writeValueAsString(initialRequestDto);
        ResultActions registerResult = mockMvc.perform(post("/api/resume")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(initialContent));

        HttpApiResponse<ResumeResponseDto> response = objectMapper.readValue(registerResult.andReturn().getResponse().getContentAsString()
                , new TypeReference<>() {});
        Long resumeId = response.data().getResumeId();

        //When
        ResultActions result = mockMvc.perform(delete("/api/resume/{resumeId}", resumeId)
                .header("Authorization", "Bearer " + token)
        );

        //Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이력서 삭제 성공"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());

        mockMvc.perform(get("/api/resume/{resumeId}", resumeId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

}
