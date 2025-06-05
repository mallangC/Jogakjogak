package com.zb.jogakjogak.resume.domain;

import com.zb.jogakjogak.resume.entity.Resume;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResumeResponseDto {
    private Long resumeId;
    private String name;
    private String content;

    public ResumeResponseDto(Resume resume) {
        this.resumeId = resume.getId();
        this.name = resume.getName();
        this.content = resume.getContent();
    }
}
