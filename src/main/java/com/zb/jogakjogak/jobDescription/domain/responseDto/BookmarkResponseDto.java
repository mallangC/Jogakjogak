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
public class BookmarkResponseDto {
    private Long jd_id;
    private boolean isBookmark;
}
