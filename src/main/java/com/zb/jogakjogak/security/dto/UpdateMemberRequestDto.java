package com.zb.jogakjogak.security.dto;


import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMemberRequestDto {

    @Size(min = 4, max = 12, message = "닉네임은 최소 4자 이상, 최대 12자 이하이어야 합니다.")
    private String nickname;
    private Boolean isNotificationEnabled;

}
