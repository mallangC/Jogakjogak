package com.zb.jogakjogak.resume.domain;

import com.zb.jogakjogak.resume.domain.requestDto.ResumeRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ResumeRequestDtoTest {
    private static Validator validator;

    /**
     * 모든 테스트가 실행되기 전에 한 번만 실행됩니다.
     * Validator 인스턴스를 초기화합니다.
     */
    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @DisplayName("이력서 내용 5000자 초과 시 유효성 검사 실패")
    @Test
    void testContentSizeExceedsLimit() {
        // Given
        String longContent = "A".repeat(5001);

        ResumeRequestDto requestDto = ResumeRequestDto.builder()
                .name("유효한 이름")
                .content(longContent)
                .build();

        // When
        Set<ConstraintViolation<ResumeRequestDto>> violations = validator.validate(requestDto);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<ResumeRequestDto> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("content");
        assertThat(violation.getMessage()).isEqualTo("이력서는 5000자 이내여야 합니다.");
    }

    @DisplayName("이력서 내용 5000자 이내(경계값 포함) 시 유효성 검사 성공")
    @Test
    void testContentSizeWithinLimit() {
        // Given
        String validContent = "B".repeat(5000);

        ResumeRequestDto requestDto = ResumeRequestDto.builder()
                .name("유효한 이름")
                .content(validContent)
                .build();

        // When
        Set<ConstraintViolation<ResumeRequestDto>> violations = validator.validate(requestDto);

        // Then
        assertThat(violations).isEmpty();
    }

    @DisplayName("이력서 이름 공백 문자열 시 유효성 검사 실패 (@NotBlank)")
    @Test
    void testNameIsBlank() {
        // Given
        ResumeRequestDto requestDto = ResumeRequestDto.builder()
                .name("")
                .content("유효한 내용입니다.")
                .build();

        // When
        Set<ConstraintViolation<ResumeRequestDto>> violations = validator.validate(requestDto);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<ResumeRequestDto> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
        assertThat(violation.getMessage()).isEqualTo("이력서 이름은 필수 입력 사항입니다.");
    }

    @DisplayName("이력서 이름 null 시 유효성 검사 실패 (@NotBlank)")
    @Test
    void testNameIsNull() {
        // Given
        ResumeRequestDto requestDto = ResumeRequestDto.builder()
                .name(null) // null 값
                .content("유효한 내용입니다.")
                .build();

        // When
        Set<ConstraintViolation<ResumeRequestDto>> violations = validator.validate(requestDto);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<ResumeRequestDto> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
        assertThat(violation.getMessage()).isEqualTo("이력서 이름은 필수 입력 사항입니다.");
    }

    @DisplayName("모든 필드가 유효할 때 유효성 검사 성공")
    @Test
    void testAllFieldsValid() {
        // Given
        ResumeRequestDto requestDto = ResumeRequestDto.builder()
                .name("완벽한 이름")
                .content("이것은 유효한 길이의 이력서 내용입니다.")
                .build();

        // When
        Set<ConstraintViolation<ResumeRequestDto>> violations = validator.validate(requestDto);

        // Then
        assertThat(violations).isEmpty();
    }
}