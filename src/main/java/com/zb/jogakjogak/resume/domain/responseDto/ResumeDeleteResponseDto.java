package com.zb.jogakjogak.resume.domain.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResumeDeleteResponseDto {
    private Long resumeId;
}
