package com.zb.jogakjogak.resume.domain.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResumeRequestDto {
    @NotBlank(message = "이력서 제목은 필수 입력 사항입니다.")
    @Size(max = 30, message = "이력서 제목은 30자 이내여야 합니다.")
    private String title;
    @NotBlank(message = "이력서 내용은 필수 입력 사항입니다.")
    @Size(max = 5000, message = "이력서는 5000자 이내여야 합니다.")
    private String content;
}
