package com.zb.jogakjogak.jobDescription.domain.requestDto;

import com.zb.jogakjogak.global.validation.MeaningfulText;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

@Schema(description = "JD 분석 요청 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JDRequestDto {
    @Schema(description = "생성할 분석 내용의 제목", example = "J사 백엔드 취업 해보자!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(max = 30, message = "제목의 최대 길이는 30자입니다.")
    private String title;
    @Schema(description = "채용 공고의 URL", example = "https://jogakjogak.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "채용 공고 URL은 필수 입력 항목입니다.")
    @URL(message = "유효한 URL 형식이 아닙니다.")
    private String jdUrl;
    @Schema(description = "채용 공고의 회사명", example = "Jogakjogak", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "회사 이름은 필수 항목입니다.")
    private String companyName;
    @Schema(description = "지원하는 직무명", example = "백엔드 개발자", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "직무 이름은 필수 항목입니다.")
    private String job;
    @Schema(description = "채용 공고 내용", example =
            "조각조각은 [취준생에게 JD와 이력서를 분석해주고 취업을 돕는 서비스]를 제공하며 빠르게 성장하고 있는 IT 스타트업입니다. 우리는 기술을 통해 **'취업을 위한 조각을 완성하는 즐거움'**이라는 새로운 가치를 만들어가고 있습니다. 함께 뛰어난 서비스를 만들며 성장할 열정적인 백엔드 개발자님을 모십니다." +
            "주요 업무" +
            "조각조각 서비스의 백엔드 API 설계 및 개발: 새로운 기능과 서비스를 뒷받침하는 견고하고 확장 가능한 API를 설계하고 개발합니다." +
            "대용량 트래픽 처리 시스템 설계 및 성능 개선: 커뮤니티와 커머스 기능이 결합된 서비스의 특성상 발생하는 대용량 트래픽을 안정적으로 처리할 수 있도록 시스템을 최적화합니다." +
            "데이터베이스 모델링 및 쿼리 최적화: 사용자 활동 데이터와 상품 데이터를 효율적으로 관리하기 위한 데이터베이스 스키마를 설계하고 쿼리 성능을 개선합니다." +
            "MSA(Microservices Architecture) 기반 시스템 개발: 핵심 비즈니스 로직을 마이크로서비스로 분리하여 유연하고 독립적인 개발 환경을 구축합니다." +
            "DevOps 환경 구축 및 자동화: CI/CD 파이프라인을 구축하고 인프라를 자동화하여 개발 생산성을 높입니다." +
            "자격 요건" +
            "실무 경험 3년 이상 또는 이에 준하는 실력을 갖추신 분 Spring Boot, Nest.js, Django, Express.js 등 한 가지 이상의 백엔드 프레임워크 활용 경험 RESTful API 설계 및 개발 경험" +
            "RDBMS(MySQL, PostgreSQL 등) 또는 NoSQL(MongoDB, Redis 등) 사용 경험 Git을 사용한 협업 경험 테스트 코드 작성 및 리팩토링에 대한 이해" +
            "우대 사항" +
            "Docker, Kubernetes 등 컨테이너 환경 기반 서비스 개발 및 운영 경험 AWS, GCP, Azure 등 클라우드 플랫폼 환경에서 서비스 배포 및 운영 경험" +
            "Kafka, RabbitMQ 등 메시지 큐 시스템 활용 경험 대용량 트래픽 처리 및 분산 시스템 설계 경험 MSA 환경에서 백엔드 개발 경험" +
            "테스트 자동화 및 CI/CD 파이프라인 구축 경험 오픈 소스 활동 참여 경험 또는 기술 블로그 운영 경험" +
            "혜택 및 복지" +
            "자율 출퇴근제: 코어 타임(10:00~16:00)을 제외한 자율 출퇴근으로 유연하게 일할 수 있습니다. 최신 장비 지원: 맥북 프로 또는 동급의 개발 장비와 듀얼 모니터를 제공합니다." +
            "성장 지원: 도서 구매비, 온라인 강의 수강료, 세미나 참가비 등 자기 계발을 위한 비용을 지원합니다. 식사/간식 지원: 점심 식비 지원 및 스낵바, 최고급 커피 머신이 항시 준비되어 있습니다." +
            "리프레시 휴가: 장기 근속자에게는 유급 휴가와 휴가비를 제공하여 재충전의 기회를 드립니다." +
            "채용 절차" +
            "서류 전형: 이력서 및 경력 기술서 코딩 테스트: 알고리즘 및 코딩 역량 평가 1차 면접: 직무 역량 및 기술 면접 2차 면접: 문화 적합성 및 최종 면접" +
            "지원 방법" +
            "제출 서류: 자유 형식의 이력서, 경력 기술서 지원 경로: [채용 플랫폼 링크, 이메일 주소 등] 조각조각에 관심 있는 분들의 많은 지원 바랍니다. 감사합니다.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "채용 공고는 필수 항목입니다.")
    @MeaningfulText(message = "채용공고 내용이 유효하지 않거나 의미 없는 반복 문자를 포함합니다.")
    @Size(min = 300, message = "채용공고의 내용은 300자 이상이어야 합니다.")
    private String content;
    @Schema(description = "채용 공고의 마감일", example = "2025-06-22T10:30:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime endedAt;
}
