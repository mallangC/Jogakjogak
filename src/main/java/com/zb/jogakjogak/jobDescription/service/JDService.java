package com.zb.jogakjogak.jobDescription.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.zb.jogakjogak.global.exception.JDErrorCode;
import com.zb.jogakjogak.global.exception.JDException;
import com.zb.jogakjogak.jobDescription.domain.requestDto.JDRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.ToDoListDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.JDDeleteResponseDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.JDResponseDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListResponseDto;
import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.jobDescription.entity.ToDoList;
import com.zb.jogakjogak.jobDescription.repsitory.JDRepository;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JDService {

    private final OpenAIResponseService openAIResponseService;
    private final ObjectMapper objectMapper;
    private final JDRepository jdRepository;
    private final LLMService llmService;
    private String resumeContent = """
            ## 이력서
            **이름:** 홍길동
            **연락처:** 010-XXXX-XXXX
            **이메일:** hong.gildong@example.com
            
            **학력:**
            * 한국대학교 컴퓨터공학과 졸업 (2020년 2월)
            
            **경력:**
            * **(주)ABC 테크 | 백엔드 개발자 (2021년 3월 ~ 현재)**
                * Python 기반의 RESTful API 개발 및 유지보수 (Django 프레임워크 사용)
                * PostgreSQL 데이터베이스 설계 및 쿼리 최적화 수행
                * Docker를 활용한 개발 환경 구축 및 배포 자동화 참여 (CI/CD 파이프라인 일부 기여)
                * 사내 프로젝트 3건 성공적 완료에 기여
            
            **기술 스택:**
            * **언어:** Python, JavaScript (기본)
            * **프레임워크:** Django, Flask (기본)
            * **데이터베이스:** PostgreSQL, MySQL (기본)
            * **클라우드/DevOps:** Docker, Git, Linux
            * **협업 도구:** Jira, Confluence, Slack
            
            **개인 프로젝트:**
            * 웹 기반 메모 애플리케이션 개발 (Django, PostgreSQL)
                * 사용자 인증, CRUD 기능 구현
                * Git으로 버전 관리
            """;
    private String jdContent = """
            ## 채용 공고: 시니어 백엔드 개발자
            
            **회사:** (주)InnovateX
            
            **주요 업무:**
            * 대규모 트래픽을 처리하는 확장 가능한 백엔드 시스템 설계, 개발 및 운영
            * Java, Spring Boot 기반의 마이크로서비스 아키텍처 구축 및 관리
            * AWS 클라우드 환경에서 서비스 배포 및 운영 자동화 (CI/CD 파이프라인 구축)
            * 데이터베이스(관계형/NoSQL) 설계 및 성능 튜닝
            * 팀원들과 협업하여 기술 문제 해결 및 코드 품질 향상 (코드 리뷰 참여)
            
            **자격 요건:**
            * 5년 이상의 백엔드 개발 경력
            * **Java 및 Spring Boot 프레임워크에 대한 깊은 이해와 실제 프로젝트 경험 필수**
            * RESTful API 설계 및 개발 능력
            * 관계형 데이터베이스(PostgreSQL, MySQL) 및 **NoSQL 데이터베이스(Redis, MongoDB 등) 사용 경험**
            * **AWS 또는 Google Cloud Platform (GCP) 사용 경험**
            * Git, Docker, Kubernetes(K8s) 활용 능력
            * 대규모 분산 시스템 설계 및 구축 경험 우대
            
            **우대 사항:**
            * CI/CD 파이프라인 구축 및 운영 경험 (Jenkins, GitLab CI 등)
            * 메시지 큐(Kafka, RabbitMQ) 사용 경험
            * 테스트 코드 작성 및 TDD(Test Driven Development) 경험
            * 우수한 커뮤니케이션 및 협업 능력
            """;

    /**
     * open ai를 이용하여 JD와 이력서를 분석하여 To Do List를 만들어주는 서비스 메서드
     *
     * @param jdRequestDto 제목, JD의 URL, 마감일
     * @return 제목, JD의 URL, To Do List, 사용자 메모, 마감일
     */
    public JDResponseDto analyze(JDRequestDto jdRequestDto) {
        // TODO: 이력서를 받아야 함. JD를 어떻게 전달 받을 지 고민 중....
        String analysisJsonString = openAIResponseService.sendRequest(resumeContent, jdContent, 4000);
        List<ToDoListDto> parsedAnalysisResult;
        try {
            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, ToDoListDto.class);
            parsedAnalysisResult = objectMapper.readValue(analysisJsonString, listType);
        } catch (JsonProcessingException e) {
            throw new JDException(JDErrorCode.FAILED_JSON_PROCESS);
        }

        JD jd = JD.builder()
                .title(jdRequestDto.getTitle())
                .jdUrl(jdRequestDto.getJDUrl())
                .endedAt(jdRequestDto.getEndedAt())
                .memo("")
                .companyName(jdRequestDto.getCompanyName())
                .job(jdRequestDto.getJob())
                .content(jdRequestDto.getContent())
                .isAlarmOn(false)
                .build();

        for (ToDoListDto dto : parsedAnalysisResult) {
            ToDoList toDoList = ToDoList.fromDto(dto, jd);
            jd.addToDoList(toDoList);
        }

        JD savedJd = jdRepository.save(jd);

        List<ToDoListResponseDto> responseToDoLists = savedJd.getToDoLists().stream()
                .map(ToDoListResponseDto::fromEntity)
                .collect(Collectors.toList());

        return JDResponseDto.builder()
                .jd_id(savedJd.getId())
                .title(savedJd.getTitle())
                .companyName(savedJd.getCompanyName())
                .job(savedJd.getJob())
                .content(savedJd.getContent())
                .jdUrl(savedJd.getJdUrl())
                .memo(savedJd.getMemo())
                .isAlarmOn(savedJd.isAlarmOn())
                .applyAt(savedJd.getApplyAt())
                .endedAt(savedJd.getEndedAt())
                .createdAt(savedJd.getCreatedAt())
                .updatedAt(savedJd.getUpdatedAt())
                .toDoLists(responseToDoLists)
                .build();

    }

    /**
     * gemini ai를 이용하여 JD와 이력서를 분석하여 To Do List를 만들어주는 서비스 메서드
     *
     * @param jdRequestDto 제목, JD의 URL, 마감일
     * @return 제목, JD의 URL, To Do List, 사용자 메모, 마감일
     */
    public JDResponseDto llmAnalyze(JDRequestDto jdRequestDto) {
        String analysisJsonString = llmService.generateTodoListJson(resumeContent, jdContent);
        List<ToDoListDto> parsedAnalysisResult;
        try {
            parsedAnalysisResult = objectMapper.readValue(analysisJsonString, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new JDException(JDErrorCode.FAILED_JSON_PROCESS);
        }

        JD jd = JD.builder()
                .title(jdRequestDto.getTitle())
                .companyName(jdRequestDto.getCompanyName())
                .job(jdRequestDto.getJob())
                .content(jdRequestDto.getContent())
                .jdUrl(jdRequestDto.getJDUrl())
                .endedAt(jdRequestDto.getEndedAt())
                .memo("")
                .build();

        for (ToDoListDto dto : parsedAnalysisResult) {
            if (dto.getCategory() == null) {
                System.err.println("경고: LLM 응답에서 ToDoList category가 누락되었습니다. 기본값으로 설정합니다.");
                dto.setCategory(ToDoListType.STRUCTURAL_COMPLEMENT_PLAN);
            }
            if (dto.getTitle() == null || dto.getTitle().isEmpty()) {
                System.err.println("경고: LLM 응답에서 ToDoList title이 누락되었습니다. 기본값으로 설정합니다.");
                dto.setTitle("제목 없음");
            }
            if (dto.getContent() == null || dto.getContent().isEmpty()) {
                System.err.println("경고: LLM 응답에서 ToDoList content가 누락되었습니다. 기본값으로 설정합니다.");
                dto.setContent("내용 없음");
            }

            ToDoList toDoList = ToDoList.fromDto(dto, jd);
            jd.addToDoList(toDoList);
        }
        JD savedJd = jdRepository.save(jd);

        List<ToDoListResponseDto> responseToDoLists = savedJd.getToDoLists().stream()
                .map(ToDoListResponseDto::fromEntity)
                .collect(Collectors.toList());

        return JDResponseDto.builder()
                .jd_id(savedJd.getId())
                .title(savedJd.getTitle())
                .companyName(savedJd.getCompanyName())
                .job(savedJd.getJob())
                .content(savedJd.getContent())
                .jdUrl(savedJd.getJdUrl())
                .memo(savedJd.getMemo())
                .isAlarmOn(savedJd.isAlarmOn())
                .applyAt(savedJd.getApplyAt())
                .endedAt(savedJd.getEndedAt())
                .createdAt(savedJd.getCreatedAt())
                .updatedAt(savedJd.getUpdatedAt())
                .toDoLists(responseToDoLists)
                .build();
    }

    /**
     * JD 분석 내용 단건 조회하는 서비스 메서드
     * @param jdId 조회하려는 jd의 아이디
     * @return 조회된 jd의 응답 dto
     */
    public JDResponseDto getJd(Long jdId) {
        JD jd = jdRepository.findByIdWithToDoLists(jdId)
                .orElseThrow(() -> new JDException(JDErrorCode.JD_NOT_FOUND));
        return JDResponseDto.fromEntity(jd);
    }

    public JDDeleteResponseDto deleteJd(Long jdId) {
        JD jd = jdRepository.findById(jdId).orElseThrow(
                () -> new JDException(JDErrorCode.JD_NOT_FOUND)
        );
        jdRepository.deleteById(jdId);
        return JDDeleteResponseDto.builder()
                .jd_id(jdId)
                .build();
    }
}
