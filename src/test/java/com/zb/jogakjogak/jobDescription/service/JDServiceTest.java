package com.zb.jogakjogak.jobDescription.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.github.javafaker.Faker;
import com.zb.jogakjogak.global.exception.JDErrorCode;
import com.zb.jogakjogak.global.exception.JDException;
import com.zb.jogakjogak.jobDescription.domain.requestDto.JDRequestDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.JDResponseDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListDto;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JDServiceTest {

    @Mock
    private OpenAIResponseService openAIResponseService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ToDoListService toDoListService;

    @InjectMocks
    private JDService jdService;

    private JDRequestDto jdRequestDto;
    private String mockAnalysisJsonString;
    private List<ToDoListDto> mockToDoList;
    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();

        jdRequestDto = JDRequestDto.builder()
                .title("시니어 백엔드 개발자 채용")
                .JDUrl("http://example.com/jd/123")
                .endedAt(LocalDateTime.of(2025, 6, 30, 12, 0))
                .build();

        mockAnalysisJsonString = "[{\"item\":\"Java 학습\",\"status\":\"TODO\"},{\"item\":\"Spring Boot 프로젝트 경험 쌓기\",\"status\":\"IN_PROGRESS\"}]";

        mockToDoList = Arrays.asList(
                new ToDoListDto(ToDoListType.CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL, faker.book().title(), "Java 학습"),
                new ToDoListDto(ToDoListType.STRUCTURAL_COMPLEMENT_PLAN, faker.book().title(), "spring boot 협업")
        );
    }

    @Test
    @DisplayName("JD 분석 서비스 성공 테스트")
    void analyze_success() throws JsonProcessingException {
        // given
        when(openAIResponseService.sendRequest(anyString(), anyString(), eq(0)))
                .thenReturn(mockAnalysisJsonString);

        when(objectMapper.getTypeFactory()).thenReturn(mock(com.fasterxml.jackson.databind.type.TypeFactory.class));
        when(objectMapper.getTypeFactory().constructCollectionType(eq(List.class), eq(ToDoListDto.class)))
                .thenReturn(mock(CollectionType.class));
        when(objectMapper.readValue(eq(mockAnalysisJsonString), any(CollectionType.class)))
                .thenReturn(mockToDoList);

        // when
        JDResponseDto result = jdService.analyze(jdRequestDto);

        // then
        assertNotNull(result);
        assertEquals(jdRequestDto.getTitle(), result.getTitle());
        assertEquals(jdRequestDto.getJDUrl(), result.getJdUrl());
        assertEquals(jdRequestDto.getEndedAt(), result.getEndedAt());
        assertFalse(result.getAnalysisResult().isEmpty());
        assertEquals(2, result.getAnalysisResult().size());
        assertEquals("Java 학습", result.getAnalysisResult().get(0).getDescription());
        assertEquals("Java 학습", result.getAnalysisResult().get(0).getDescription());

        // verify
        verify(openAIResponseService, times(1)).sendRequest(anyString(), anyString(), eq(0));
        verify(objectMapper, times(1)).readValue(eq(mockAnalysisJsonString), any(CollectionType.class));
    }

    @Test
    @DisplayName("JD 분석 서비스 JsonProcessingException 발생 시 JDException 던지는지 테스트")
    void analyze_jsonProcessingException() throws JsonProcessingException {
        // given
        when(openAIResponseService.sendRequest(anyString(), anyString(), eq(0)))
                .thenReturn("invalid json string");

        // objectMapper가 JsonProcessingException을 던지도록 설정
        when(objectMapper.getTypeFactory()).thenReturn(mock(com.fasterxml.jackson.databind.type.TypeFactory.class));
        when(objectMapper.getTypeFactory().constructCollectionType(eq(List.class), eq(ToDoListDto.class)))
                .thenReturn(mock(CollectionType.class));
        when(objectMapper.readValue(anyString(), any(CollectionType.class)))
                .thenThrow(mock(JsonProcessingException.class)); // 실제 JsonProcessingException 인스턴스를 모의

        // when & then
        JDException thrown = assertThrows(JDException.class, () -> jdService.analyze(jdRequestDto));
        assertEquals(JDErrorCode.FAILED_JSON_PROCESS, thrown.getErrorCode());

        // verify
        verify(openAIResponseService, times(1)).sendRequest(anyString(), anyString(), eq(0));
        verify(objectMapper, times(1)).readValue(anyString(), any(CollectionType.class));
    }
}