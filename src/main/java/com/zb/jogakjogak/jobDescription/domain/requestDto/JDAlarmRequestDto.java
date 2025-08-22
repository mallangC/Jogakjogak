package com.zb.jogakjogak.jobDescription.domain.requestDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "분석 알림 설정 요청DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JDAlarmRequestDto {
    @Schema(description = "분석 알림 설정 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("isAlarmOn")
    private boolean isAlarmOn;
}
