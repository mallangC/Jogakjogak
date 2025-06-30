package com.zb.jogakjogak.jobDescription.domain.responseDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "분석 메모 응답 DTO")
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class MemoResponseDto {
    @Schema(description = "분석 아이디", example = "1")
    private Long jd_id;
    @Schema(description = "메모", example = "1일차 - 조각 3개 완료, 2일차 - 조각 2개 완료")
    private String memo;
}
