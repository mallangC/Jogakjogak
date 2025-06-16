package com.zb.jogakjogak.jobDescription.domain.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class JDAlarmResponseDto {
    private Long jdId;
    private boolean isAlarmOn;
}
