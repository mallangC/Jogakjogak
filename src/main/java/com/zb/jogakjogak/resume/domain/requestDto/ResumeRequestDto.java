package com.zb.jogakjogak.resume.domain.requestDto;

import com.zb.jogakjogak.global.validation.MeaningfulText;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "이력서 등록 요청 DTO")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResumeRequestDto {
    @Schema(description = "이력서 제목", example = "6월 20일 이력서", requiredMode=Schema.RequiredMode.NOT_REQUIRED)
    @NotBlank(message = "이력서 제목은 필수 입력 사항입니다.")
    @Size(max = 30, message = "이력서 제목은 30자 이내여야 합니다.")
    private String title;
    @Schema(description = "이력서 내용", example =
            " 핵심 역량" +
            "언어: Java, Python" +
            "프레임워크: Spring Boot, Node.js(Express)" +
            "데이터베이스: MySQL, PostgreSQL, Redis" +
            "도구/환경: Git, Docker, Linux, REST API" +
            "기타: JPA/Hibernate, JUnit5, Flyway, Swagger" +
            " 프로젝트 경험" +
            " 1. 중고 서적 거래 플랫폼 '북마켓' (팀 프로젝트)" +
            "개발 기간: 2024.11 2025.01 (3개월)" +
            "담당 역할: 백엔드 API 개발 및 MySQL 데이터베이스 설계" +
            "프로젝트 개요: 사용자들이 중고 서적을 사고팔 수 있는 웹 기반 커머스 플랫폼입니다." +
            "주요 기능 구현:" +
            "게시판 CRUD: Spring Data JPA를 활용하여 게시물, 댓글, 거래글에 대한 RESTful API를 설계하고 개발했습니다." +
            "사용자 인증/인가: Spring Security와 JWT를 이용해 안전한 회원가입, 로그인, 권한 관리를 구현했습니다." +
            "이미지 업로드: AWS S3를 연동하여 대용량 이미지 파일을 안정적으로 저장하고 관리했습니다." +
            "사용 기술:" +
            "Backend: Spring Boot, Java 17, Spring Security, JPA/Hibernate" +
            "Database: MySQL, Flyway" +
            "Infra/Tool: Docker, Git, Swagger, AWS S3" +
            "Github 링크: [https://github.com/minjun-dev/book-market](https://www.google.com/search?q=https://github.com/minjun-dev/book-market)" +
            "백엔드 개발:" +
            "RESTful API 설계 및 개발에 대한 높은 이해를 가지고 있습니다." +
            "객체지향 프로그래밍(OOP) 원칙과 디자인 패턴(싱글톤, 팩토리 등)에 대한 이해를 바탕으로 깔끔한 코드를 작성합니다." +
            "테스트 코드(JUnit5) 작성 경험으로 안정적인 코드 유지보수에 기여할 수 있습니다." +
            "데이터베이스:" +
            "관계형 데이터베이스(RDBMS)의 테이블 설계 및 ERD 모델링 경험을 보유하고 있습니다." +
            "SQL 기본 문법 및 최적화된 쿼리 작성 능력을 갖추고 있습니다." +
            "인프라/협업:" +
            "Git을 활용한 버전 관리 및 협업(Pull Request/Merge)에 익숙합니다." +
            "Docker를 이용해 개발 환경을 컨테이너화하고, CI/CD 자동화에 대한 높은 관심을 가지고 있습니다." +
            "Swagger를 활용한 API 명세서 작성 경험으로 협업의 효율을 높입니다." +
            "학습 능력:" +
            "새로운 기술과 지식을 빠르게 습득하고 프로젝트에 적용하는 데 거부감이 없습니다." +
            "오류 발생 시 로그를 분석하고 원인을 파악하여 해결하는 능력을 갖추고 있습니다.",
            requiredMode=Schema.RequiredMode.NOT_REQUIRED)
    @Size(min= 300, max = 5000, message = "이력서는 300자 이상 5000자 이내여야 합니다.")
    @MeaningfulText(message = "이력서 내용이 유효하지 않거나 의미 없는 반복 문자를 포함합니다.")
    private String content;
}
