package com.zb.jogakjogak.resume.domain.requestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SkillDto {
    @NotBlank(message = "내용을 입력해주세요")
    private String content;
}
