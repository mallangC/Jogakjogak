package com.zb.jogakjogak.jobDescription.domain.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllGetJDResponseDto {
    private Long jd_id;
    private String title;
    private boolean isBookmark;
    private boolean isAlarmOn;
    private String companyName;
    private Long totalPieces;
    private Long completedPieces;
    private LocalDateTime applyAt;
    private LocalDateTime endedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
