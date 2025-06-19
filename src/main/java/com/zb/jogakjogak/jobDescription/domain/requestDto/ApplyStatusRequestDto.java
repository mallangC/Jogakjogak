package com.zb.jogakjogak.jobDescription.domain.requestDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyStatusRequestDto {
    private LocalDateTime applyAt;
}
