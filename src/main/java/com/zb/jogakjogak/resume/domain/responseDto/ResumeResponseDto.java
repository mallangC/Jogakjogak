package com.zb.jogakjogak.resume.domain.responseDto;

import com.zb.jogakjogak.resume.entity.Resume;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResumeResponseDto {
    private Long resumeId;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ResumeResponseDto(Resume resume) {
        this.resumeId = resume.getId();
        this.title = resume.getTitle();
        this.content = resume.getContent();
        this.createdAt = resume.getCreatedAt();
        this.updatedAt = resume.getUpdatedAt();
    }
}
