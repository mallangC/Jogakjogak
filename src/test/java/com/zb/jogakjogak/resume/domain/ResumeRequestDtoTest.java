package com.zb.jogakjogak.resume.domain;

import com.github.javafaker.Faker;
import com.zb.jogakjogak.resume.domain.requestDto.ResumeRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory;

import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class ResumeRequestDtoTest {

    private Validator validator;
    private Faker faker;

    /**
     * 모든 테스트가 실행되기 전에 한 번만 실행됩니다.
     * Validator 인스턴스를 초기화합니다.
     */
    @BeforeEach
    void setUp() {
        faker = new Faker();

        ApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        ((AnnotationConfigApplicationContext) applicationContext).register(
                com.zb.jogakjogak.global.validation.EnglishWordDictionary.class,
                com.zb.jogakjogak.global.validation.MeaningfulTextValidator.class
        );
        ((AnnotationConfigApplicationContext) applicationContext).refresh();

        ValidatorFactory validatorFactory = Validation.byDefaultProvider()
                .configure()
                .constraintValidatorFactory(new SpringConstraintValidatorFactory(applicationContext.getAutowireCapableBeanFactory()))
                .buildValidatorFactory();

        this.validator = validatorFactory.getValidator();
    }

    @DisplayName("이력서 내용 5000자 초과 시 유효성 검사 실패")
    @Test
    void testContentSizeExceedsLimit() {
        // Given
        StringBuilder longContentBuilder = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longContentBuilder.append(faker.lorem().paragraph());
            longContentBuilder.append(" ");
        }

        String longContent = longContentBuilder.toString();
        if (longContent.length() < 5001) {
            longContent = faker.lorem().sentence(500).repeat(10);
            if(longContent.length() < 5001) {
                longContent += faker.lorem().characters(5001 - longContent.length());
            }
        }
        longContent = longContent.substring(0, 5001);

        ResumeRequestDto requestDto = ResumeRequestDto.builder()
                .title("테스트의 이력서.")
                .content(longContent)
                .build();

        // When
        Set<ConstraintViolation<ResumeRequestDto>> violations = validator.validate(requestDto);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<ResumeRequestDto> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("content");
        assertThat(violation.getMessage()).isEqualTo("이력서는 300자 이상 5000자 이내여야 합니다.");
    }

    @DisplayName("이력서 내용 5000자 이내(경계값 포함) 시 유효성 검사 성공")
    @Test
    void testContentSizeWithinLimit() {
        // Given
        StringBuilder longContentBuilder = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longContentBuilder.append(faker.lorem().paragraph());
            longContentBuilder.append(" ");
        }

        String validContent = longContentBuilder.toString();
        if (validContent.length() < 5001) {
            validContent = faker.lorem().sentence(500).repeat(10);
            if(validContent.length() < 5001) {
                validContent += faker.lorem().characters(5001 - validContent.length());
            }
        }
        validContent = validContent.substring(0, 5000);


        ResumeRequestDto requestDto = ResumeRequestDto.builder()
                .title("유효한 이름")
                .content(validContent)
                .build();

        // When
        Set<ConstraintViolation<ResumeRequestDto>> violations = validator.validate(requestDto);

        // Then
        assertThat(violations).isEmpty();
    }

    @DisplayName("이력서 내용 공백시 유효성 검사 실패")
    @Test
    void testContentIsBlank() {
        // Given
        ResumeRequestDto requestDto = ResumeRequestDto.builder()
                .title("유효한 이름")
                .content("")
                .build();

        // When
        Set<ConstraintViolation<ResumeRequestDto>> violations = validator.validate(requestDto);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<ResumeRequestDto> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("content");
        assertThat(violation.getMessage()).isEqualTo("이력서는 300자 이상 5000자 이내여야 합니다.");
    }

    @DisplayName("이력서 이름 공백 문자열 시 유효성 검사 실패 (@NotBlank)")
    @Test
    void testNameIsBlank() {
        // Given
        StringBuilder contentBuilder = new StringBuilder();
        while (contentBuilder.length() < 300) {
            contentBuilder.append(faker.lorem().paragraph(2));
            contentBuilder.append(" "); // 단락 사이에 공백 추가
        }
        String validContent = contentBuilder.toString();
        ResumeRequestDto requestDto = ResumeRequestDto.builder()
                .title("")
                .content(validContent)
                .build();

        // When
        Set<ConstraintViolation<ResumeRequestDto>> violations = validator.validate(requestDto);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<ResumeRequestDto> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("title");
        assertThat(violation.getMessage()).isEqualTo("이력서 제목은 필수 입력 사항입니다.");
    }

    @DisplayName("이력서 이름 null 시 유효성 검사 실패 (@NotBlank)")
    @Test
    void testNameIsNull() {
        // Given
        StringBuilder contentBuilder = new StringBuilder();
        while (contentBuilder.length() < 300) {
            contentBuilder.append(faker.lorem().paragraph(2));
            contentBuilder.append(" ");
        }
        String validContent = contentBuilder.toString();
        ResumeRequestDto requestDto = ResumeRequestDto.builder()
                .title(null)
                .content(validContent)
                .build();

        // When
        Set<ConstraintViolation<ResumeRequestDto>> violations = validator.validate(requestDto);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<ResumeRequestDto> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("title");
        assertThat(violation.getMessage()).isEqualTo("이력서 제목은 필수 입력 사항입니다.");
    }

    @DisplayName("이력서 제목이 30자 초과시 유효성 검사 실패")
    @Test
    void testNameSizeExceedsLimit() {
        // Given
        StringBuilder contentBuilder = new StringBuilder();
        while (contentBuilder.length() < 300) {
            contentBuilder.append(faker.lorem().paragraph(2));
            contentBuilder.append(" "); // 단락 사이에 공백 추가
        }
        String validContent = contentBuilder.toString();
        String longTitle = faker.lorem().paragraph(30);

        ResumeRequestDto requestDto = ResumeRequestDto.builder()
                .title(longTitle)
                .content(validContent)
                .build();

        // When
        Set<ConstraintViolation<ResumeRequestDto>> violations = validator.validate(requestDto);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<ResumeRequestDto> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("title");
        assertThat(violation.getMessage()).isEqualTo("이력서 제목은 30자 이내여야 합니다.");
    }

    @DisplayName("모든 필드가 유효할 때 유효성 검사 성공")
    @Test
    void testAllFieldsValid() {
        // Given
        StringBuilder contentBuilder = new StringBuilder();
        while (contentBuilder.length() < 300) {
            contentBuilder.append(faker.lorem().paragraph(2));
            contentBuilder.append(" "); // 단락 사이에 공백 추가
        }
        String validContent = contentBuilder.toString();
        ResumeRequestDto requestDto = ResumeRequestDto.builder()
                .title("완벽한 이름")
                .content(validContent)
                .build();

        // When
        Set<ConstraintViolation<ResumeRequestDto>> violations = validator.validate(requestDto);

        // Then
        assertThat(violations).isEmpty();
    }
}