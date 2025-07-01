package com.zb.jogakjogak.jobDescription.domain.responseDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "지원 완료/미완료 설정 응답 DTO")
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ApplyStatusResponseDto {
    @Schema(description = "분석 아이디",example = "1")
    private Long jd_id;
    @Schema(description = "지원 완료 시간",example = "2025-06-22T10:30:00Z")
    private LocalDateTime applyAt;
}
