package com.zb.jogakjogak.jobDescription.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import com.zb.jogakjogak.global.exception.CustomJDErrorCode;
import com.zb.jogakjogak.global.exception.CustomJDException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class OpenAIResponseService {

    private final Logger logger = LoggerFactory.getLogger(OpenAIResponseService.class);
    private final OpenAiService openAiService;
    private final String model;
    private final int defaultMaxTokens;

    public OpenAIResponseService(
            @Value("${openai.api.key}") String apiKey,
            @Value("${openai.api.model:gpt-4}") String model,
            @Value("${openai.api.max-tokens:1000}") int maxTokens) {
        // theokanning.openai.service.OpenAiService는 API 키와 타임아웃을 인자로 받습니다.
        // 여기서는 기본 타임아웃 30초를 설정했습니다. 필요에 따라 조정하세요.
        this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(30));
        this.model = model;
        this.defaultMaxTokens = maxTokens;
    }

    public String sendRequest(String resume, String jobDescription, Integer maxTokens) {
        //TODO: PROMPT 좋은 걸로 수정해야 함.
        String systemContent = """
                당신은 제공된 이력서와 채용 공고를 보고 To do List를 작성해 주는 AI입니다.
                1. 구조적 보완 계획, 2.내용 강조/ 재구성 재안(표현 및 피드백 기반), 3.취업 일정 관련\s
                이 세가지로 나누어 To do List를 작성해 주세요. 만약 정보가 제공되지 않으면 작성하지 않으셔도 됩니다.""";

        ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemContent);
        ChatMessage resumeMessage = new ChatMessage(ChatMessageRole.USER.value(), "<이력서>\n" + resume + "\n</이력서>");
        ChatMessage jdMessage = new ChatMessage(ChatMessageRole.USER.value(), "<채용 공고>\n" + jobDescription + "\n</채용 공고>");

        int usedMaxTokens = (maxTokens != null && maxTokens > 0) ? maxTokens : this.defaultMaxTokens;

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(this.model)
                .messages(List.of(systemMessage, resumeMessage, jdMessage))
                .temperature(0.7)
                .maxTokens(usedMaxTokens)
                .n(1)
                .build();

        try {
            ChatCompletionResult result = openAiService.createChatCompletion(request);
            logger.info("OpenAI API Chat Completion Result: {}", result);

            if (result != null && !result.getChoices().isEmpty()) {
                return result.getChoices().get(0).getMessage().getContent().trim();
            } else {
                throw new CustomJDException(CustomJDErrorCode.FAILED_ANALYSIS_REQUEST);
            }
        } catch (Exception e) {
            logger.error("OpenAI API 호출 중 오류 발생: {}", e.getMessage());
            return "요청 처리에 실패했습니다. 오류가 발생했습니다.";
        }
    }
}