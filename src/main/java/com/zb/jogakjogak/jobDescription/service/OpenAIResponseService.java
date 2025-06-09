package com.zb.jogakjogak.jobDescription.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.zb.jogakjogak.global.exception.JDErrorCode;
import com.zb.jogakjogak.global.exception.JDException;
import com.zb.jogakjogak.jobDescription.domain.requestDto.MessageDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.OpenAIRequestDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.OpenAIResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;

@Service
public class OpenAIResponseService {

    private final Logger logger = LoggerFactory.getLogger(OpenAIResponseService.class);
    private final RestClient restClient;
    private final String model;
    private final int defaultMaxTokens;
    private final ObjectMapper objectMapper;

    public OpenAIResponseService(
            @Value("${openai.api.key}") String apiKey,
            @Value("${openai.api.model:gpt-4}") String model,
            @Value("${openai.api.max-tokens:1000}") int maxTokens,
            @Value("${openai.api.url}") String openaiApiUrl,
            ObjectMapper objectMapper,
            RestClient.Builder restClientBuilder) {
        this.model = model;
        this.defaultMaxTokens = maxTokens;
        this.objectMapper = objectMapper;

        // RestClient 초기화
        this.restClient = restClientBuilder
                .baseUrl(openaiApiUrl) // API URL을 baseUrl로 설정
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String sendRequest(String resume, String jobDescription, Integer maxTokens) {
        String systemContent = """
                당신은 제공된 이력서와 채용 공고를 보고 To do List를 작성해 주는 AI입니다.
                응답은 오직 JSON 배열 형식으로만 제공되어야 합니다.
                각 배열 항목은 다음 필드를 포함하는 JSON 객체여야 합니다:
                - 'type': "구조적 보완 계획", "내용 강조/ 재구성 재안(표현 및 피드백 기반)", "취업 일정 관련" 중 하나.
                - 'description': 해당 To-Do Item의 한글 설명.
                - 'priority': '높음', '중간', '낮음' 중 하나.
                
                **각 'type'에 대해 최소 2개에서 최대 3개의 To-Do Item을 생성해주세요.**
                만약 관련 정보가 충분하지 않으면 해당 유형의 To-Do Item은 0개 또는 1개만 포함해도 되지만, 가능한 경우 여러 개를 생성하도록 노력해야 합니다.
                
                예시 JSON 응답:
                [
                  {
                    "type": "구조적 보완 계획",
                    "description": "이력서에 프로젝트 경험을 최신 기술 스택 위주로 재구성하고, GitHub 링크를 명확히 추가하여 코드 역량을 강조합니다.",
                    "priority": "높음"
                  },
                  {
                    "type": "구조적 보완 계획",
                    "description": "자기소개서의 성장 과정을 직무 역량과 연결하여 서술하고, 기업의 인재상에 맞춰 내용을 보완합니다.",
                    "priority": "중간"
                  },
                  {
                    "type": "내용 강조/ 재구성 재안(표현 및 피드백 기반)",
                    "description": "채용 공고의 '클라우드 경험' 요구사항에 맞춰, AWS EC2 사용 경험을 구체적인 수치와 함께 서술하여 업무 기여도를 명확히 합니다.",
                    "priority": "높음"
                  },
                  {
                    "type": "내용 강조/ 재구성 재안(표현 및 피드백 기반)",
                    "description": "과거 프로젝트에서 발생했던 문제점과 해결 과정을 구체적인 데이터와 함께 서술하여 문제 해결 능력을 강조합니다.",
                    "priority": "중간"
                  },
                  {
                    "type": "취업 일정 관련",
                    "description": "A사 서류 마감일(YY/MM/DD)에 맞춰 이력서 최종 검토 및 제출을 완료하고, 면접 예상 질문 리스트를 작성합니다.",
                    "priority": "높음"
                  },
                  {
                    "type": "취업 일정 관련",
                    "description": "B사 코딩 테스트 대비를 위해 프로그래머스 고득점 키트를 활용하여 알고리즘 문제 풀이 연습을 진행합니다.",
                    "priority": "중간"
                  }
                ]
                """;

        MessageDto systemMessage = new MessageDto(ChatMessageRole.SYSTEM.value(), systemContent);
        MessageDto resumeMessage = new MessageDto(ChatMessageRole.USER.value(), "<이력서>\n" + resume + "\n</이력서>");
        MessageDto jdMessage = new MessageDto(ChatMessageRole.USER.value(), "<채용 공고>\n" + jobDescription + "\n</채용 공고>");

        int usedMaxTokens = (maxTokens != null && maxTokens > 0) ? maxTokens : this.defaultMaxTokens;

        OpenAIRequestDto requestDto = new OpenAIRequestDto(
                this.model,
                List.of(systemMessage, resumeMessage, jdMessage),
                0.7,
                usedMaxTokens
        );

        try {
            OpenAIResponseDto openaiResponse = restClient.post()
                    .uri("/chat/completions")
                    .body(requestDto)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), (req, res) -> {
                        logger.error("Client Error: {} - {}", res.getStatusCode(), res.getStatusText());
                        throw new JDException(JDErrorCode.INVALID_API_REQUEST);
                    })
                    .onStatus(status -> status.is5xxServerError(), (req, res) -> {
                        logger.error("Server Error: {} - {}", res.getStatusCode(), res.getStatusText());
                        throw new JDException(JDErrorCode.API_SERVER_ERROR);
                    })
                    .body(OpenAIResponseDto.class);

            logger.info("OpenAI API Chat Completion Result: {}", openaiResponse);

            if (openaiResponse != null && !openaiResponse.getChoices().isEmpty()) {
                String jsonContent = Objects.requireNonNull(openaiResponse.getChoices().get(0).getMessage()).getContent().trim();
                logger.debug("Received JSON Content from OpenAI: {}", jsonContent);

                return jsonContent;

            } else {
                throw new JDException(JDErrorCode.FAILED_ANALYSIS_REQUEST);
            }
        } catch (JDException e) {
            throw e;
        } catch (Exception e) {
            logger.error("OpenAI API 호출 중 오류 발생: {}", e.getMessage(), e);
            return "요청 처리에 실패했습니다. 오류가 발생했습니다.";
        }
    }
}