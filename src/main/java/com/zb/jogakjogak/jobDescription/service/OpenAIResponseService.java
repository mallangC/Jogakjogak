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
                - 'type': 다음 영문 Enum 상수 이름 중 하나여야 합니다: "STRUCTURAL_COMPLEMENT_PLAN", "CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL", "EMPLOYMENT_SCHEDULE_RELATED".
                  각 Enum 상수는 다음과 같은 의미를 가집니다:
                  - STRUCTURAL_COMPLEMENT_PLAN: 구조적 보완 계획
                  - CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL: 내용 강조/재구성 제안(표현 및 피드백 기반)
                  - EMPLOYMENT_SCHEDULE_RELATED: 취업 일정 관련
                - 'title': 해당 To-Do 항목의 간략하고 명확한 제목 (예: "이력서 프로젝트 경험 재구성", "클라우드 경험 구체화", "코딩 테스트 대비")
                - 'description': 해당 To-Do Item의 한글 설명.
                - 'memo': To-Do Item에 대한 추가 사용자 메모입니다. 항상 빈 문자열("")로 설정해주세요.
                - 'isDone': To-Do Item의 완료 여부입니다. 항상 false로 설정해주세요.
                
                **각 'type'에 대해 최소 2개에서 최대 3개의 To-Do Item을 생성해주세요.**
                만약 관련 정보가 충분하지 않으면 해당 유형의 To-Do Item은 0개 또는 1개만 포함해도 되지만, 가능한 경우 여러 개를 생성하도록 노력해야 합니다.
                
                예시 JSON 응답:
                [
                  {
                    "type": "STRUCTURAL_COMPLEMENT_PLAN", // <-- 영문 Enum 상수 이름 사용
                    "title": "이력서 프로젝트 경험 재구성",
                    "description": "이력서에 나열된 프로젝트 경험들을 채용 공고의 요구 기술 스택과 관련된 최신 프로젝트 위주로 재구성하고, 각 프로젝트에 대한 GitHub 저장소 링크를 명확히 추가하여 실제 코드 구현 역량을 강조합니다.",
                    "memo": "",
                    "isDone": false
                  },
                  {
                    "type": "CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL", // <-- 영문 Enum 상수 이름 사용
                    "title": "AWS 클라우드 경험 구체화",
                    "description": "채용 공고에서 요구하는 '클라우드 경험'에 맞춰, 과거 AWS EC2를 활용한 서비스 배포 경험을 구체적인 수치(예: '트래픽 처리량 20% 개선', '배포 시간 15% 단축')와 함께 서술하여 실제 업무 기여도를 명확히 강조합니다.",
                    "memo": "",
                    "isDone": false
                  },
                  {
                    "type": "EMPLOYMENT_SCHEDULE_RELATED", // <-- 영문 Enum 상수 이름 사용
                    "title": "A사 서류 제출 및 면접 준비",
                    "description": "A사의 서류 마감일(2025/07/15)에 맞춰 이력서와 자기소개서의 최종 검토 및 제출을 완료하고, 예상 면접 질문 리스트를 작성하여 답변을 준비합니다.",
                    "memo": "",
                    "isDone": false
                  },
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
                //TODO: ai분 석 내용 확인용이므로 나중에 삭제하거나 다른 거로 수정해야함.
                logger.info("Received JSON Content from OpenAI: {}", jsonContent);

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