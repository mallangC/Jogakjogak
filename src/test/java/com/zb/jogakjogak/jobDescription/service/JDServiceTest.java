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

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    private String mockLLMAnalysisJsonString;
    private List<ToDoListDto> mockToDoListDtosForLLM;

    @BeforeEach
    void setUp() {
        Faker faker = new Faker();

        jdRequestDto = JDRequestDto.builder()
                .title("시니어 백엔드 개발자 채용")
                .JDUrl("https://example.com/jd/123")
                .companyName(faker.company().name())
                .job(faker.job().title())
                .content(faker.lorem().paragraph())
                .endedAt(faker.date().future(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .build();

        mockLLMAnalysisJsonString = "[" +
                "  {" +
                "    \"category\": \"STRUCTURAL_COMPLEMENT_PLAN\"," +
                "    \"title\": \"이력서 Java/Spring Boot 경험 강조\"," +
                "    \"content\": \"이력서에 Spring Boot 프로젝트 경험을 구체적으로 서술합니다.\"," +
                "    \"memo\": \"\"," +
                "    \"isDone\": false" +
                "  }," +
                "  {" +
                "    \"category\": \"CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL\"," +
                "    \"title\": \"AWS 클라우드 경험 구체화\"," +
                "    \"content\": \"AWS EC2 배포 경험을 수치와 함께 명확히 기술합니다.\"," +
                "    \"memo\": \"\"," +
                "    \"isDone\": false" +
                "  }" +
                "]";

        ToDoListDto llmDto1 = ToDoListDto.builder()
                .category(ToDoListType.STRUCTURAL_COMPLEMENT_PLAN)
                .title("이력서 Java/Spring Boot 경험 강조")
                .content("이력서에 Spring Boot 프로젝트 경험을 구체적으로 서술합니다.") // description -> content
                .memo("")
                .isDone(false)
                .build();
        ToDoListDto llmDto2 = ToDoListDto.builder()
                .category(ToDoListType.CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL)
                .title("AWS 클라우드 경험 구체화")
                .content("AWS EC2 배포 경험을 수치와 함께 명확히 기술합니다.") // description -> content
                .memo("")
                .isDone(false)
                .build();
        mockToDoListDtosForLLM = Arrays.asList(llmDto1, llmDto2);
    }

    @Test
    @DisplayName("JD 분석 서비스 성공 테스트 - JD 및 ToDoList 저장 포함")
    void analyze_success() throws JsonProcessingException {
        // Given
        String openAIAssumedJson = "[{\"category\":\"STRUCTURAL_COMPLEMENT_PLAN\"," +
                "\"title\":\"OpenAI Test Title\"," +
                "\"content\":\"OpenAI Test Content\"," +
                "\"memo\":\"\",\"isDone\":false}]";
        ToDoListDto openAIToDo1 = ToDoListDto.builder()
                .category(ToDoListType.STRUCTURAL_COMPLEMENT_PLAN)
                .title("OpenAI Test Title")
                .content("OpenAI Test Content")
                .memo("")
                .isDone(false)
                .build();
        List<ToDoListDto> openAIToDoListDtos = Collections.singletonList(openAIToDo1);


        when(openAIResponseService.sendRequest(anyString(), anyString(), anyInt()))
                .thenReturn(openAIAssumedJson); // LLMService의 JSON과 동일하게 변경

        when(objectMapper.readValue(eq(openAIAssumedJson), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(openAIToDoListDtos);
        when(jdRepository.save(any(JD.class))).thenAnswer(invocation -> {
            JD originalJd = invocation.getArgument(0);
            // ID mocking
            return JD.builder()
                    .id(1L) // ID mocking
                    .title(originalJd.getTitle())
                    .companyName(originalJd.getCompanyName())
                    .job(originalJd.getJob())
                    .content(originalJd.getContent())
                    .jdUrl(originalJd.getJdUrl())
                    .endedAt(originalJd.getEndedAt())
                    .memo(originalJd.getMemo())
                    .isAlarmOn(originalJd.isAlarmOn())
                    .applyAt(originalJd.getApplyAt())
                    .toDoLists(originalJd.getToDoLists())
                    .build();
        });


        // when
        JDResponseDto result = jdService.analyze(jdRequestDto);

        // then
        assertNotNull(result);
        assertEquals(jdRequestDto.getTitle(), result.getTitle());
        assertEquals(jdRequestDto.getJDUrl(), result.getJdUrl());
        assertEquals(jdRequestDto.getEndedAt(), result.getEndedAt());
        assertFalse(result.getToDoLists().isEmpty());
        assertEquals(openAIToDoListDtos.size(), result.getToDoLists().size());
        assertEquals("OpenAI Test Content", result.getToDoLists().get(0).getContent());
        assertEquals(ToDoListType.STRUCTURAL_COMPLEMENT_PLAN, result.getToDoLists().get(0).getCategory());
        assertEquals("", result.getToDoLists().get(0).getMemo());
        assertFalse(result.getToDoLists().get(0).isDone());

        // verify
        verify(objectMapper, times(1)).readValue(eq(openAIAssumedJson), any(com.fasterxml.jackson.core.type.TypeReference.class));
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
            return JD.builder()
                    .id(1L)
                    .title(originalJd.getTitle())
                    .companyName(originalJd.getCompanyName())
                    .job(originalJd.getJob())
                    .content(originalJd.getContent())
                    .jdUrl(originalJd.getJdUrl())
                    .endedAt(originalJd.getEndedAt())
                    .memo(originalJd.getMemo())
                    .isAlarmOn(originalJd.isAlarmOn())
                    .applyAt(originalJd.getApplyAt())
                    .toDoLists(originalJd.getToDoLists())
                    .build();
        });

        // when
        JDResponseDto result = jdService.llmAnalyze(jdRequestDto);

        // then
        assertNotNull(result);
        assertEquals(jdRequestDto.getTitle(), result.getTitle());
        assertEquals(jdRequestDto.getJDUrl(), result.getJdUrl());
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
        verify(llmService, times(1)).generateTodoListJson(anyString(), anyString());
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
