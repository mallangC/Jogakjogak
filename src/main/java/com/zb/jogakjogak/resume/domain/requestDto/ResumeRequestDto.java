package com.zb.jogakjogak.resume.domain.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ResumeRequestDto {
    @NotBlank(message = "이력서 이름은 필수 입력 사항입니다.")
    private String name;
    @Size(max = 5000, message = "이력서는 5000자 이내여야 합니다.")
    private String content;
}
