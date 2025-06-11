package com.zb.jogakjogak.jobDescription.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.github.javafaker.Faker;
import com.zb.jogakjogak.global.exception.JDErrorCode;
import com.zb.jogakjogak.global.exception.JDException;
import com.zb.jogakjogak.jobDescription.domain.requestDto.JDRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.ToDoListDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.JDResponseDto;
import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.jobDescription.entity.ToDoList;
import com.zb.jogakjogak.jobDescription.repsitory.JDRepository;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
    private LLMService llmService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private JDService jdService;

    @Mock
    private JDRepository jdRepository;

    private JDRequestDto jdRequestDto;
    private String mockAnalysisJsonString;
    private String mockLLMAnalysisJsonString;
    private List<ToDoListDto> mockToDoListDtos;
    private List<ToDoListDto> mockToDoListDtosForLLM;
    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();

        jdRequestDto = JDRequestDto.builder()
                .title("시니어 백엔드 개발자 채용")
                .JDUrl("http://example.com/jd/123")
                .endedAt(LocalDateTime.of(2050, 6, 11, 12, 0))
                .build();

        mockAnalysisJsonString = "[{\"item\":\"Java 학습\",\"status\":\"TODO\"},{\"item\":\"Spring Boot 프로젝트 경험 쌓기\",\"status\":\"IN_PROGRESS\"}]";

        mockToDoListDtos = Arrays.asList(
                new ToDoListDto(ToDoListType.CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL, faker.book().title(), "Java 학습"),
                new ToDoListDto(ToDoListType.STRUCTURAL_COMPLEMENT_PLAN, faker.book().title(), "spring boot 협업")
        );

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

    }

    @Test
    @DisplayName("JD 분석 서비스 성공 테스트 - JD 및 ToDoList 저장 포함")
    void analyze_success() throws JsonProcessingException {
        // given
        when(openAIResponseService.sendRequest(anyString(), anyString(), anyInt()))
                .thenReturn(mockAnalysisJsonString);

        when(objectMapper.getTypeFactory()).thenReturn(mock(com.fasterxml.jackson.databind.type.TypeFactory.class));
        when(objectMapper.getTypeFactory().constructCollectionType(eq(List.class), eq(ToDoListDto.class)))
                .thenReturn(mock(CollectionType.class));
        when(objectMapper.readValue(eq(mockAnalysisJsonString), any(CollectionType.class)))
                .thenReturn(mockToDoListDtos);
        when(jdRepository.save(any(JD.class))).thenAnswer(invocation -> {
            JD originalJd = invocation.getArgument(0);
            JD savedJdMock = mock(JD.class);
            when(savedJdMock.getTitle()).thenReturn(originalJd.getTitle());
            when(savedJdMock.getJdUrl()).thenReturn(originalJd.getJdUrl());
            when(savedJdMock.getEndedAt()).thenReturn(originalJd.getEndedAt());
            when(savedJdMock.getMemo()).thenReturn(originalJd.getMemo());
            return savedJdMock;
        });

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
        assertEquals("", result.getAnalysisResult().get(0).getMemo());
        assertFalse(result.getAnalysisResult().get(0).isDone());


        // verify
        verify(objectMapper, times(1)).readValue(eq(mockAnalysisJsonString), any(CollectionType.class));
        verify(jdRepository, times(1)).save(any(JD.class));
    }

    @Test
    @DisplayName("JD 분석 서비스 JsonProcessingException 발생 시 JDException 던지는지 테스트")
    void analyze_jsonProcessingException() throws JsonProcessingException {
        // given
        when(openAIResponseService.sendRequest(anyString(), anyString(), anyInt()))
                .thenReturn("이것은 잘못된 JSON 형식의 문자열입니다.");

        when(objectMapper.getTypeFactory()).thenReturn(mock(com.fasterxml.jackson.databind.type.TypeFactory.class));
        when(objectMapper.getTypeFactory().constructCollectionType(eq(List.class), eq(ToDoListDto.class)))
                .thenReturn(mock(CollectionType.class));
        when(objectMapper.readValue(anyString(), any(CollectionType.class)))
                .thenThrow(mock(JsonProcessingException.class));

        // when & then
        JDException thrown = assertThrows(JDException.class, () -> jdService.analyze(jdRequestDto));
        assertEquals(JDErrorCode.FAILED_JSON_PROCESS, thrown.getErrorCode());

        // verify
        verify(objectMapper, times(1)).readValue(anyString(), any(CollectionType.class));
        verify(jdRepository, never()).save(any(JD.class));
    }

    @Test
    @DisplayName("LLM 분석 서비스 성공 테스트 - JD 및 ToDoList 저장 포함 (Gemini)")
    void llmAnalyze_success() throws JsonProcessingException {
        // given
        when(llmService.generateTodoListJson(anyString(), anyString()))
                .thenReturn(mockLLMAnalysisJsonString);

        when(objectMapper.readValue(eq(mockLLMAnalysisJsonString), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(mockToDoListDtosForLLM);

        when(jdRepository.save(any(JD.class))).thenAnswer(invocation -> {
            JD originalJd = invocation.getArgument(0);
            for (ToDoListDto dto : mockToDoListDtosForLLM) {
                ToDoList toDoList = ToDoList.fromDto(dto, originalJd);
                originalJd.addToDoList(toDoList);
            }
            return originalJd;
        });

        // when
        JDResponseDto result = jdService.llmAnalyze(jdRequestDto);

        // then
        assertNotNull(result);
        assertEquals(jdRequestDto.getTitle(), result.getTitle());
        assertEquals(jdRequestDto.getJDUrl(), result.getJdUrl());
        assertEquals(jdRequestDto.getEndedAt(), result.getEndedAt());
        assertFalse(result.getAnalysisResult().isEmpty());
        assertEquals(mockToDoListDtosForLLM.size(), result.getAnalysisResult().size()); // LLM용 DTO 사이즈

        // LLM 응답 DTO 내용 검증
        assertEquals(mockToDoListDtosForLLM.get(0).getTitle(), result.getAnalysisResult().get(0).getTitle());
        assertEquals(mockToDoListDtosForLLM.get(0).getDescription(), result.getAnalysisResult().get(0).getDescription());
        assertEquals(mockToDoListDtosForLLM.get(0).getType(), result.getAnalysisResult().get(0).getType());
        assertEquals(mockToDoListDtosForLLM.get(0).isDone(), result.getAnalysisResult().get(0).isDone());

        // verify
        verify(objectMapper, times(1)).readValue(eq(mockLLMAnalysisJsonString), any(com.fasterxml.jackson.core.type.TypeReference.class));
        verify(jdRepository, times(1)).save(any(JD.class));

        ArgumentCaptor<JD> jdCaptor = ArgumentCaptor.forClass(JD.class);
        verify(jdRepository).save(jdCaptor.capture());
        JD savedJd = jdCaptor.getValue();
        assertNotNull(savedJd.getToDoLists());
        assertEquals(mockToDoListDtosForLLM.get(0).getTitle(), savedJd.getToDoLists().get(0).getTitle());
    }

    @Test
    @DisplayName("LLM 분석 서비스 JsonProcessingException 발생 시 JDException 던지는지 테스트 (Gemini)")
    void llmAnalyze_failure_jsonProcessingException() throws JsonProcessingException {
        // given
        when(llmService.generateTodoListJson(anyString(), anyString()))
                .thenReturn("invalid json string from LLM");

        when(objectMapper.readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenThrow(mock(JsonProcessingException.class));

        // when & then
        JDException thrown = assertThrows(JDException.class, () -> jdService.llmAnalyze(jdRequestDto));
        assertEquals(JDErrorCode.FAILED_JSON_PROCESS, thrown.getErrorCode());

        // verify
        verify(llmService, times(1)).generateTodoListJson(anyString(), anyString());
        verify(objectMapper, times(1)).readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class));
        verify(jdRepository, never()).save(any(JD.class));
    }
}
