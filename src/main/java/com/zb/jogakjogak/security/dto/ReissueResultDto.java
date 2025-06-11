package com.zb.jogakjogak.security.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReissueResultDto {

    private String newRefreshToken;
    private String newAccessToken;
}
