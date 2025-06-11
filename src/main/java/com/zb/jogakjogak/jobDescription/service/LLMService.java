package com.zb.jogakjogak.jobDescription.service;

import com.google.common.collect.ImmutableMap;
import com.google.genai.Client;
import com.google.genai.types.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LLMService {

    @Value("${gemini.api.key}")
    private String API_KEY;
    private final String MODEL_NAME = "gemini-2.0-flash";


    public String generateTodoListJson(String resumeContent, String jobDescriptionContent) {

        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new IllegalStateException("Gemini API 키가 이상합니다 확인해주세요.");
        }

        try (Client client = Client.builder()
                .apiKey(API_KEY)
                .build()) {

            String SYSTEM_INSTRUCTION_TEXT = """
                    당신은 제공된 이력서와 채용 공고를 보고 To do List를 작성해 주는 AI입니다.
                    응답은 어떠한 추가 설명이나 서론/결론 없이 오직 JSON 배열 형식으로만 제공되어야 합니다.
                    각 배열 항목은 다음 필드를 포함하는 JSON 객체여야 합니다:
                    - 'category': 다음 영문 Enum 상수 이름 중 하나여야 합니다: \"STRUCTURAL_COMPLEMENT_PLAN\", \"CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL\", \"EMPLOYMENT_SCHEDULE_RELATED\".
                      각 Enum 상수는 다음과 같은 의미를 가집니다:
                      - STRUCTURAL_COMPLEMENT_PLAN: 구조적 보완 계획
                      - CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL: 내용 강조/재구성 제안(표현 및 피드백 기반)
                      - EMPLOYMENT_SCHEDULE_RELATED: 취업 일정 관련
                    - 'title': 해당 To-Do 항목의 간략하고 명확한 제목 (예: \"이력서 프로젝트 경험 재구성\", \"클라우드 경험 구체화\", \"코딩 테스트 대비\")
                    - 'content': 해당 To-Do Item의 한글 설명. **이 설명은 지원자가 실제 취해야 할 구체적인 행동, 학습 내용, 강조할 포인트, 예상 결과 등을 포함하여 상세하고 명확하며, 최소 30자 이상의 충분한 길이로 작성되어야 합니다.**
                    - 'memo': To-Do Item에 대한 추가 사용자 메모입니다. 항상 빈 문자열(\"\")로 설정해주세요.
                    - 'isDone': To-Do Item의 완료 여부입니다. 항상 false로 설정해주세요.

                    **각 'category'에 대해 최소 1개에서 최대 10개의 To-Do Item을 생성해주세요.**
                    만약 관련 정보가 충분하지 않으면 해당 유형의 To-Do Item은 0개 또는 1개만 포함해도 되지만, 가능한 경우 여러 개를 생성하도록 노력해야 합니다.
                    
                    예시 JSON 응답:
                    [
                      {
                        \"category\": \"STRUCTURAL_COMPLEMENT_PLAN\",
                        \"title\": \"이력서 프로젝트 경험 재구성\",
                        \"description\": \"이력서에 나열된 프로젝트 경험들을 채용 공고의 요구 기술 스택과 관련된 최신 프로젝트 위주로 재구성하고, 각 프로젝트에 대한 GitHub 저장소 링크를 명확히 추가하여 실제 코드 구현 역량을 강조합니다. 특히, 프로젝트 목표, 본인의 역할, 사용 기술, 달성한 성과(수치화 가능한 경우)를 상세하게 기술하여 인사 담당자가 지원자의 기여도를 명확히 파악할 수 있도록 돕습니다. 최소 30자 이상으로 작성되어야 합니다.\",
                        \"memo\": \"\",
                        \"isDone\": false
                      },
                      {
                        \"category\": \"CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL\",
                        \"title\": \"AWS 클라우드 경험 구체화\",
                        \"description\": \"채용 공고에서 요구하는 '클라우드 경험'에 맞춰, 과거 AWS EC2를 활용한 서비스 배포 경험을 구체적인 수치(예: '트래픽 처리량 20% 개선', '배포 시간 15% 단축')와 함께 서술하여 실제 업무 기여도를 명확히 강조합니다. AWS S3, Lambda, DynamoDB 등 다른 관련 서비스에 대한 이해도와 활용 경험이 있다면 이를 추가하여 클라우드 전반의 역량을 어필합니다. 최소 30자 이상으로 작성되어야 합니다.\",
                        \"memo\": \"\",
                        \"isDone\": false
                      },
                      {
                        \"category\": \"EMPLOYMENT_SCHEDULE_RELATED\",
                        \"title\": \"A사 서류 제출 및 면접 준비\",
                        \"description\": \"A사의 서류 마감일(2025/07/15)에 맞춰 이력서와 자기소개서의 최종 검토 및 제출을 완료하고, 예상 면접 질문 리스트를 작성하여 답변을 준비합니다. 특히, 회사의 비전, 주요 서비스, 최근 기술 동향 등을 면밀히 조사하여 면접 시 회사에 대한 깊은 이해와 관심을 보여줄 수 있도록 합니다. 모의 면접을 통해 답변의 논리성 및 전달력을 점검하는 것도 중요합니다. 최소 30자 이상으로 작성되어야 합니다.\",
                        \"memo\": \"\",
                        \"isDone\": false
                      },
                    ]
                    """;

            // 사용자 프롬프트 구성
            String userPromptContent = String.format(
                    "이력서: %s\n채용 공고: %s\n\n위 이력서와 채용 공고를 기반으로, 지원자가 부족한 부분을 보완하고 채용 공고에 더 잘 맞출 수 있도록 돕는 To-Do 리스트를 JSON 형식으로 생성해 주세요.",
                    resumeContent,
                    jobDescriptionContent
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

            // 응답에서 텍스트 추출
            return response.text();

        } catch (Exception e) { // IOException 대신 일반 Exception으로 변경합니다.
            System.err.println("Error generating content from LLM: " + e.getMessage());
            e.printStackTrace();
            return "{\"error\": \"Failed to generate To-Do list due to an LLM error.\"}";
        }
    }
}