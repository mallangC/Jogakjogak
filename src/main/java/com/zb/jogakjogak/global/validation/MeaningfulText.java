package com.zb.jogakjogak.global.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MeaningfulTextValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MeaningfulText {
    String message() default "입력 내용이 유효하지 않거나 의미 없는 반복 문자를 포함합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
