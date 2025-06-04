package com.zb.jogakjogak.resume.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResumeResponseDto {
    private int resumeId;
    private String name;
    private String content;
}
