package com.zb.jogakjogak.jobDescription.domain.requestDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "분석 알림 설정 요청DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JDAlarmRequestDto {
    @Schema(description = "분석 알림 설정 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    public boolean isAlarmOn;
}
