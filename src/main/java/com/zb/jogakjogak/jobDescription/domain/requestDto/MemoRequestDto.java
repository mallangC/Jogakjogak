package com.zb.jogakjogak.jobDescription.domain.requestDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "분석 메모 작성 요청 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoRequestDto {
    @Schema(description = "메모", example = "1일차 - 조각 3개 완료, 2일차 - 조각 2개 완료", requiredMode = Schema.RequiredMode.REQUIRED)
    private String memo;
}
