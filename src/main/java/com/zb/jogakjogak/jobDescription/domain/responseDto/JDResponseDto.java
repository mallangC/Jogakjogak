package com.zb.jogakjogak.jobDescription.domain.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class JDResponseDto {
    private String title;
    private String jdUrl;
    private String analysisResult;
    private String memo;
    private LocalDateTime endedAt;
}
