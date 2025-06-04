package com.zb.jogakjogak.resume.domain;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ResumeRequestDto {
    private String name;
    private String content;
}
