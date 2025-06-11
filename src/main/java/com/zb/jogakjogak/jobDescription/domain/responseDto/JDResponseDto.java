package com.zb.jogakjogak.jobDescription.domain.responseDto;

import com.zb.jogakjogak.jobDescription.domain.requestDto.ToDoListDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class JDResponseDto {
    private String title;
    private String jdUrl;
    private List<ToDoListDto> analysisResult;
    private String memo;
    private LocalDateTime endedAt;
}
