package com.zb.jogakjogak.jobDescription.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.genai.Client;
import com.google.genai.types.*;
import com.zb.jogakjogak.global.exception.AIServiceException;
import com.zb.jogakjogak.global.exception.JDErrorCode;
import com.zb.jogakjogak.global.exception.JDException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class LLMService {

    @Value("${gemini.api.key}")
    private String API_KEY;
    private final String MODEL_NAME = "gemini-2.0-flash";


    public String generateTodoListJson(String resumeContent, String jobDescriptionContent, String jobName) {

        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new IllegalStateException("Gemini API 키가 이상합니다 확인해주세요.");
        }

        try (Client client = Client.builder()
                .apiKey(API_KEY)
                .build()) {

            String SYSTEM_INSTRUCTION_TEXT = """
                    당신은 사용자가 제공하는 이력서와 채용 공고(JD)를 분석하여, 합격률을 높이는 데 필요한 개인화된 To-Do List를 작성하는 전문 AI 어시스턴트입니다.
                    당신은 마치 지원자의 '과외 선생님'처럼, 직접적이고 명확하며 행동을 유도하는 조언을 '해요체'로 제공해야 합니다. 비전문가도 쉽게 이해할 수 있는 언어를 사용하세요.
                    
                    **응답은 어떠한 추가 설명, 서론, 결론 없이 오직 JSON 배열 형식으로만 제공되어야 합니다.**
                    각 배열 항목은 다음 필드를 포함하는 JSON 객체여야 합니다:
                    
                    - 'category': 다음 영문 Enum 상수 이름 중 하나여야 합니다:
                      - "STRUCTURAL_COMPLEMENT_PLAN" (구조적 보완 계획): 이력서나 자기소개서의 형식, 구성, 필수 정보 누락 등 구조적인 부분을 보완하는 계획.
                      - "CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL" (내용 강조/재구성 제안): 이력서/자기소개서의 내용 중 JD에 맞춰 강조하거나 재구성해야 할 부분, 혹은 실제 경험을 기반으로 한 구체적인 피드백.
                      - "SCHEDULE_MISC_ERROR" (취업 일정 관련 및 기타 ): 코딩 테스트 준비, 면접 준비, 추가 학습 등 취업 일정과 관련된 계획.
                    
                    - 'title': 해당 To-Do 항목의 간략하고 명확한 제목입니다. **명령형 동사로 시작하는 행동 중심의 키워드 구문이어야 하며, 무조건 공백 포함 50자 이하로 작성하세요(가장 중요: 이 제약을 반드시 지켜야 합니다. 50자를 초과할 경우 데이터베이스 저장에 실패합니다.).** (예: "이력서 프로젝트 경험 재구성", "클라우드 경험 구체화", "코딩 테스트 대비") 질문형이나 설명형 문구는 금지됩니다.
                    
                    - 'content': 해당 To-Do Item의 한글 설명입니다. **이 설명은 지원자가 실제 취해야 할 구체적인 행동, 학습 내용, 강조할 포인트, 예상 결과 등을 포함하여 상세하고 명확하며, 최소 30자 이상, 최대 250자 이내의 충분한 길이로 작성되어야 합니다.** 최대한 비전문가도 알 수 있도록 쉽게 설명하며, 필요한 경우 설명에 숫자 기반의 성과 지표(예: 매출, 사용자 수 등)를 포함하여 구체성을 높이세요. 이 설명은 독립적인 문장으로 구성되어 투두팁이나 상세 모달에 바로 쓸 수 있는 형태여야 합니다.
                    
                    - 'memo': To-Do Item에 대한 추가 사용자 메모입니다. **항상 빈 문자열("")로 설정해주세요.**
                    
                    - 'isDone': To-Do Item의 완료 여부입니다. **항상 false로 설정해주세요.**
                    
                    **각 'category'에 대해 최소 1개에서 최대 10개의 To-Do Item을 생성해주세요.** (총 To-Do Item 수는 30개를 넘지 않아야 합니다.) 만약 특정 카테고리와 관련된 정보가 부족하거나 생성할 내용이 없다면, 해당 카테고리에 대한 To-Do Item은 0개 또는 1개만 포함할 수 있습니다. 가능한 경우 여러 개를 생성하도록 노력해야 합니다.
                    생성된 To-Do Item들은 중요도 또는 영향도를 기준으로 자동 정렬되어, 지원자에게 사용자 편의를 고려한 시각적 그룹을 제공해야 합니다.
                    **같은 주제의 To-Do Item은 반복하지 마세요. 유사하거나 중복되는 항목이 있다면 하나의 To-Do Item으로 병합 처리하여 제공하세요.**
                    
                    **[중요 알림]**
                    만약 제공된 이력서와 채용 공고의 내용이 너무 간략하거나 부족한 경우, AI 분석이 제한적일 수 있습니다. 이 경우, 해당 카테고리에 대한 To-Do Item이 적게 생성되거나 없을 수 있습니다. 더 정확하고 상세한 To-Do List를 원하시면, 보다 구체적인 정보를 제공해주세요.
                    
                    **예시 JSON 응답:**
                    [
                      {
                        "category": "STRUCTURAL_COMPLEMENT_PLAN",
                        "title": "이력서 프로젝트 경험 재구성",
                        "content": "이력서에 나열된 프로젝트 경험들을 채용 공고의 요구 기술 스택과 관련된 최신 프로젝트 위주로 재구성하고, 각 프로젝트에 대한 GitHub 저장소 링크를 명확히 추가하여 실제 코드 구현 역량을 강조합니다. 특히, 프로젝트 목표, 본인의 역할, 사용 기술, 달성한 성과(수치화 가능한 경우)를 상세하게 기술하여 인사 담당자가 지원자의 기여도를 명확히 파악할 수 있도록 돕습니다. 최소 30자 이상으로 작성되어야 합니다.",
                        "memo": "",
                        "isDone": false
                      },
                      {
                        "category": "CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL",
                        "title": "AWS 클라우드 경험 구체화",
                        "content": "채용 공고에서 요구하는 '클라우드 경험'에 맞춰, 과거 AWS EC2를 활용한 서비스 배포 경험을 구체적인 수치(예: '트래픽 처리량 20% 개선', '배포 시간 15% 단축')와 함께 서술하여 실제 업무 기여도를 명확히 강조합니다. AWS S3, Lambda, DynamoDB 등 다른 관련 서비스에 대한 이해도와 활용 경험이 있다면 이를 추가하여 클라우드 전반의 역량을 어필합니다. 최소 30자 이상으로 작성되어야 합니다.",
                        "memo": "",
                        "isDone": false
                      },
                      {
                        "category": "SCHEDULE_MISC_ERROR",
                        "title": "서류 제출 및 면접 준비",
                        "content": "A사의 서류 마감일(2025/07/15)에 맞춰 이력서와 자기소개서의 최종 검토 및 제출을 완료하고, 예상 면접 질문 리스트를 작성하여 답변을 준비합니다. 특히, 회사의 비전, 주요 서비스, 최근 기술 동향 등을 면밀히 조사하여 면접 시 회사에 대한 깊은 이해와 관심을 보여줄 수 있도록 합니다. 모의 면접을 통해 답변의 논리성 및 전달력을 점검하는 것도 중요합니다. 최소 30자 이상으로 작성되어야 합니다.",
                        "memo": "",
                        "isDone": false
                      }
                    ]
                    """;

            // 사용자 프롬프트 구성
            String userPromptContent = String.format(
                    "이력서: %s\n채용 공고: %s\n직무 이름: %s\n\n위 이력서, 채용 공고, 그리고 직무 이름을 기반으로, 지원자가 부족한 부분을 보완하고 해당 직무의 채용 공고에 더 잘 맞출 수 있도록 돕는 To-Do 리스트를 JSON 형식으로 생성해 주세요.",
                    resumeContent,
                    jobDescriptionContent,
                    jobName
            );

            // 메시지(Content) 구성
            List<Content> contents = List.of(
                    Content.fromParts(Part.fromText(userPromptContent))
            );

            // JSON 스키마 정의 (To-Do 리스트 JSON 형식을 따름)
            Schema taskSchema = Schema.builder()
                    .type("object")
                    .properties(
                            ImmutableMap.of(
                                    "category", Schema.builder().type(Type.Known.STRING).description("To-Do 항목의 Enum 타입입니다.").build(),
                                    "title", Schema.builder().type(Type.Known.STRING).description("To-Do 항목의 간결하고 명확한 제목입니다.").build(),
                                    "content", Schema.builder().type(Type.Known.STRING).description("To-Do 항목에 대한 상세한 한글 설명입니다.").build(),
                                    "memo", Schema.builder().type(Type.Known.STRING).description("추가 사용자 메모입니다. 항상 빈 문자열로 설정해주세요.").build(),
                                    "isDone", Schema.builder().type(Type.Known.BOOLEAN).description("To-Do 항목의 완료 여부입니다. 항상 false로 설정해주세요.").build()
                            )
                    )
                    .required(List.of("category", "title", "content", "memo", "isDone"))
                    .build();

            Schema responseArraySchema = Schema.builder()
                    .type("array")
                    .items(taskSchema)
                    .build();

            // SafetySettings 구성
            List<SafetySetting> safetySettings = new ArrayList<>();
            safetySettings.add(SafetySetting.builder()
                    .category(HarmCategory.Known.HARM_CATEGORY_HATE_SPEECH)
                    .threshold(HarmBlockThreshold.Known.OFF)
                    .build());
            safetySettings.add(SafetySetting.builder()
                    .category(HarmCategory.Known.HARM_CATEGORY_DANGEROUS_CONTENT)
                    .threshold(HarmBlockThreshold.Known.OFF)
                    .build());
            safetySettings.add(SafetySetting.builder()
                    .category(HarmCategory.Known.HARM_CATEGORY_SEXUALLY_EXPLICIT)
                    .threshold(HarmBlockThreshold.Known.OFF)
                    .build());
            safetySettings.add(SafetySetting.builder()
                    .category(HarmCategory.Known.HARM_CATEGORY_HARASSMENT)
                    .threshold(HarmBlockThreshold.Known.OFF)
                    .build());


            // GenerateContentConfig 설정
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .temperature(1.0f)
                    .topP(1.0f)
                    .seed(0)
                    .maxOutputTokens(65535)
                    .responseMimeType("application/json")
                    .responseSchema(responseArraySchema)
                    .systemInstruction(Content.fromParts(Part.fromText(SYSTEM_INSTRUCTION_TEXT)))
                    .safetySettings(safetySettings)
                    .build();

            GenerateContentResponse response = client.models.generateContent(MODEL_NAME, contents, config);

            String responseText = response.text();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseText);

            if (rootNode.isArray()) {
                for (JsonNode node : rootNode) {
                    if (node.has("title")) {
                        String title = node.get("title").asText();
                        if (title.length() > 50) {
                            throw new JDException(JDErrorCode.FAILED_ANALYSIS_REQUEST_TEXT_LENGTH_EXCEED);
                        }
                    }
                }
            }
            // 응답에서 텍스트 추출
            return responseText;

        } catch (JDException e) {
            // JDException은 그대로 전파
            throw e;
        } catch (Exception e) {
            // 에러 메시지를 더 구체적으로 개선
            String errorMessage = "LLM 서비스 오류: ";
            
            if (e.getMessage() != null && e.getMessage().contains("API key")) {
                errorMessage = "Gemini API 키가 유효하지 않습니다";
            } else if (e.getMessage() != null && e.getMessage().contains("quota")) {
                errorMessage = "Gemini API 사용량을 초과했습니다";
            } else if (e.getMessage() != null && e.getMessage().contains("rate limit")) {
                errorMessage = "Gemini API 요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요";
            } else if (e instanceof java.net.ConnectException || e instanceof java.net.UnknownHostException) {
                errorMessage = "Gemini API 서버에 연결할 수 없습니다";
            } else if (e instanceof com.fasterxml.jackson.core.JsonProcessingException) {
                errorMessage = "AI 응답 파싱 중 오류가 발생했습니다";
            } else {
                errorMessage += e.getMessage() != null ? e.getMessage() : "알 수 없는 오류가 발생했습니다";
            }
            
            // 로그에 상세 에러 출력
            log.error("LLM Service Error: {}", e.getMessage(), e);
            
            throw new AIServiceException(errorMessage, e);
        }
    }
}