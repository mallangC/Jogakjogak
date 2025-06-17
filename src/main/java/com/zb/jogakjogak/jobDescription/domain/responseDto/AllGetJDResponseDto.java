package com.zb.jogakjogak.jobDescription.domain.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllGetJDResponseDto {
    private Long jd_id;
    private String title;
    private boolean isBookmark;
    private String companyName;
    private Long total_pieces;
    private Long completed_pieces;
    private LocalDateTime applyAt;
    private LocalDate endedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
