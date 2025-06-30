package com.zb.jogakjogak.jobDescription.domain.responseDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Schema(description = "즐겨찾기 설정/미설정 응답 DTO")
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class BookmarkResponseDto {
    @Schema(description = "분석 아이디", example = "1")
    private Long jd_id;
    @Schema(description = "즐겨찾기 설정 여부", example = "true")
    private boolean isBookmark;
}
