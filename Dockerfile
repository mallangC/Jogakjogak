#멀티 스테이지 빌드를 위한 jdk
FROM eclipse-temurin:17-jdk-jammy AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle Wrapper 및 빌드 스크립트 복사
COPY gradlew .
COPY gradle/wrapper/ /app/gradle/wrapper/

# Gradle Wrapper 실행 권한 부여
RUN chmod +x gradlew

# 의존성 파일(build.gradle, settings.gradle 등)만 먼저 복사
# 소스 코드는 의존성 이후에 복사
COPY build.gradle settings.gradle ./
RUN ./gradlew dependencies --no-daemon
COPY src ./src

# Gradle 빌드 (테스트 제외, 데몬 중지)
RUN ./gradlew build --no-daemon -x test

# 진단 스텝
RUN echo "Contents of /app/build/libs/ after build:"
RUN ls -al /app/build/libs/ || echo "/app/build/libs/ not found or empty"

# 빌드된 JAR 파일을 컨테이너 내부에 복사
COPY /app/build/libs/*.jar app.jar



# JRE만 포함된 경량 이미지 사용
FROM eclipse-temurin:17-jre-jammy

# 컨테이너의 작업 디렉토리 설정
WORKDIR /app

# 빌드 스테이지에서 생성된 JAR 파일 복사
COPY --from=builder /app/app.jar .

# 애플리케이션이 사용할 포트 노출 (문서화 목적)
EXPOSE 8080

# 컨테이너 시작 시 실행될 명령어 (JAR 파일 실행)
ENTRYPOINT ["java", "-jar", "app.jar"]