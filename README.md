# 조각조각
Gemini 2.0 모델을 사용하여 이력서, JD를 분석 후 체크리스트 형식으로 취업에 도움 되는 내용을 알려주는 서비스 입니다.

## API 명세서
https://api.jogakjogak.com/swagger-ui/index.html

## 프로젝트 기능 및 설계
- 로그인/ 회원 가입 기능
    - OAuth 2.0 라이브러리를 이용하여 구글, 카카오 로그인시 회원 가입이 함께 진행된다.
    - accessToken은 24시간, refreshToken은 7일 후 만료되고 만료 시 재발급 받아야 한다.
    - 회원정보 저장 시 비밀번호와 이메일이 암호화되어 저장된다.
    - 회원탈퇴 시 카카오, 구글과 연결끊기를 진행한다.
- 알림 기능
    - 사용자가 알림설정을 해 놓았고 채용공고의 todo list를 3일이상 최신화하지 않은 경우 독려하는 이메일 알림이 발송된다.
    - 알림은 스프링 배치 + 스케쥴러를 통해 오전10시에 일괄 발송된다.
    - 이메일 템플릿에 포함되는 로고, 아이콘 등 정적 이미지는 AWS S3에 업로드된 리소스를 사용한다.
- 이력서 관련 기능
    - 로그인한 사용자는 제목, 이력서의 내용을 적어 이력서를 등록할 수 있다.
    - 이력서의 내용은 5000자 이하여야 한다.
    - 로그인한 사용자는 하나의 이력서만 등록할 수 있다.
    - 로그인한 사용자는 이력서 수정, 조회 삭제 가능하다.
- 채용공고 관련 기능
    - 로그인한 사용자는 이력서 등록 후 채용공고의 제목, URL, 회사 이름, 직무 이름, 채용공고 내용, 마감일을 입력하여 사용자의 이력서와 채용 공고를 비교하여 보완할 부분을 todolist로 분석 받을 수 있다.
    - 채용공고의 내용은 최소 300자 이상이어야 한다.
    - 로그인한 사용자는 특정 채용공고 분석 내용, 전체 채용공고 분석 리스트 조회, 즐겨찾기 등록, 지원 완료, 메모 수정을 할 수 있다.
    - 채용 공고 분석은 20개 이상 할 수 없다.
- todolist 관련 기능
    - 채용공고 분석시 gemini 2.0을 이용하여 분석하고 답변시 ‘해요체’를 사용하여 todolist를 작성한다.
    - 채용공고 분석시 todolist의 타입은 STRUCTURAL_COMPLEMENT_PLAN("구조적 보완 계획"), CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL("내용 강조/재구성 제안(표현 및 피드백 기반)"),SCHEDULE_MISC_ERROR("일정 관리 및 기타") 3가지로 나누어 분석한다.
    - 각 todolist는 타입별로 10개를 초과할 수 없다.
    - 로그인한 사용자는 todolist를 추가, 수정, 삭제 할 수 있다.

## ERD
<img width="760" height="587" alt="Image" src="https://github.com/user-attachments/assets/7db7c6d4-0086-42b8-bbf4-5d929440100c" />

## 프로젝트 목표
다양한 팀과 협업하여 AI 기술을 활용해 취준생에게 도움되는 서비스를 제공

## 아키텍쳐 설계
- Java Spring Boot로 백엔드 구현
- 전반적인 보안은 Spring Security 프레임워크 기반으로 구현하며
  카카오톡 소셜 로그인 서비스와의 안전한 연동을 위해 OAuth 2.0를 사용
- API는 Restful API로 설계
- DB는 오픈소스이면서 빠르고 범용성이 좋은 MySQL을 사용
- 카카오톡과 Spring Batch로 알림 서비스를 구현
- Google Analytics로 사용자 패턴을 기록
- AI는 Gemini 2.0 flash 모델을 사용
- CI/CD 파이프라인은 Docker, GitHub Actions, EC2로 구축
- 도메인주소와 HTTPS, RDS등 배포에 필요한 서비스는 AWS를 이용

## 기술 스택
1. Spring JPA
2. Spring Security
3. Spring Batch
4. JWT
5. OAuth 2.0
6. MySQL
7. QueryDSL
8. Redis
9. Java Mail Sender
10. Docker
11. EC2 & ALB
12. Route 53
13. RDS
14. S3
15. Swagger