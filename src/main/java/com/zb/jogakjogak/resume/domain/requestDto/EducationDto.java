package com.zb.jogakjogak.resume.domain.requestDto;

import com.zb.jogakjogak.resume.type.EducationLevel;
import com.zb.jogakjogak.resume.type.EducationStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EducationDto {
    @NotBlank(message = "교육 수준을 입력해주세요.")
    private EducationLevel level;
    @NotBlank(message = "주요 학문 분야를 입력해주세요.")
    private String majorField;
    @NotBlank(message = "교육 상태를 입력해주세요.")
    private EducationStatus status;
}
