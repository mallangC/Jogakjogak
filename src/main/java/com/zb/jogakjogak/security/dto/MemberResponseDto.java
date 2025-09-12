package com.zb.jogakjogak.security.dto;


import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberResponseDto {

    private String nickname;
    private String email;
    private boolean isNotificationEnabled;

}
