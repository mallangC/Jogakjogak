package com.zb.jogakjogak.jobDescription.service;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OpenAIResponseServiceTest {

    @Mock
    private OpenAiService openAiService;

    private OpenAIResponseService openAIResponseService;


    private static final String MOCK_RESUME = "5년차 백엔드 개발 경험. Java, Spring Boot 능숙. RESTful API 설계 및 개발 경험 다수. MSA 환경 경험.";
    private static final String MOCK_JOB_DESCRIPTION = "백엔드 개발자 채용. Java/Spring Boot 필수. RESTful API 개발 경험 우대. MSA 경험 우대. 적극적인 문제 해결 능력.";
    private static final String EXPECTED_RESPONSE_CONTENT = "구조적 보완 계획: ...\n내용 강조/재구성 제안: ...\n취업 일정 관련 To do List: ...";
    private static final String DEFAULT_RESPONSE = "요청 처리에 실패했습니다. 다시 시도해 주세요.";

    @BeforeEach
    void setUp() {
        openAIResponseService = new OpenAIResponseService(
                "test-api-key",
                "gpt-4",
                1000
        );
        ReflectionTestUtils.setField(openAIResponseService, "openAiService", openAiService);
    }

    private ChatCompletionResult createSuccessResult(String content) {
        ChatMessage mockResponseMessage = new ChatMessage("assistant", content);
        ChatCompletionChoice mockChoice = new ChatCompletionChoice();
        mockChoice.setMessage(mockResponseMessage);
        ChatCompletionResult mockResult = new ChatCompletionResult();
        mockResult.setChoices(Collections.singletonList(mockChoice));
        return mockResult;
    }

    @Test
    @DisplayName("sendRequest 성공")
    void sendRequest_success_returnsExpectedContent() {
        // Given
        int maxTokens = 500;
        when(openAiService.createChatCompletion(any(ChatCompletionRequest.class)))
                .thenReturn(createSuccessResult(EXPECTED_RESPONSE_CONTENT));

        // When
        String actualResponse = openAIResponseService.sendRequest(MOCK_RESUME, MOCK_JOB_DESCRIPTION, maxTokens);

        // Then
        assertEquals(EXPECTED_RESPONSE_CONTENT.trim(), actualResponse);
    }

    @Test
    @DisplayName("sendRequest 실패 - sendRequest_choices가 비어 있을 때")
    void sendRequest_failure_emptyChoicesReturnsFailureMessage() {
        // Given
        int maxTokens = 500;
        ChatCompletionResult mockResult = new ChatCompletionResult();
        mockResult.setChoices(Collections.emptyList());
        when(openAiService.createChatCompletion(any(ChatCompletionRequest.class)))
                .thenReturn(mockResult);

        // When
        String actualResponse = openAIResponseService.sendRequest(MOCK_RESUME, MOCK_JOB_DESCRIPTION, maxTokens);

        // Then
        assertEquals(DEFAULT_RESPONSE, actualResponse);
    }

    @Test
    @DisplayName("sendRequest 실패 - sendRequest_결과가 null일 때_실패 메시지를 반환한다")
    void sendRequest_failure_nullResultReturnsFailureMessage() {
        // Given
        int maxTokens = 500;
        when(openAiService.createChatCompletion(any(ChatCompletionRequest.class)))
                .thenReturn(null);

        // When
        String actualResponse = openAIResponseService.sendRequest(MOCK_RESUME, MOCK_JOB_DESCRIPTION, maxTokens);

        // Then
        assertEquals(DEFAULT_RESPONSE, actualResponse);
    }

    @Test
    @DisplayName("sendRequest_maxTokens이 null 또는 0일 때_기본 토큰을 사용")
    void sendRequest_useDefaultMaxTokens_returnsExpectedContent() {
        // Given
        String defaultResponseContent = "기본 토큰 응답";
        ReflectionTestUtils.setField(openAIResponseService, "defaultMaxTokens", 500);
        when(openAiService.createChatCompletion(any(ChatCompletionRequest.class)))
                .thenReturn(createSuccessResult(defaultResponseContent));

        // When
        String actualResponseWithNullMaxTokens = openAIResponseService.sendRequest(MOCK_RESUME, MOCK_JOB_DESCRIPTION, null);
        String actualResponseWithZeroMaxTokens = openAIResponseService.sendRequest(MOCK_RESUME, MOCK_JOB_DESCRIPTION, 0);

        // Then
        assertEquals(defaultResponseContent.trim(), actualResponseWithNullMaxTokens);
        assertEquals(defaultResponseContent.trim(), actualResponseWithZeroMaxTokens);
    }
}