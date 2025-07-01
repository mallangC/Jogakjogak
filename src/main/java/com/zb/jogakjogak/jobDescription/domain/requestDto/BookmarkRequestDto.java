package com.zb.jogakjogak.jobDescription.domain.requestDto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "즐겨찾기 설정 요청 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkRequestDto {
    @Schema(description = "즐겨찾기 설정 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("isBookmark")
    private boolean isBookmark;
}
