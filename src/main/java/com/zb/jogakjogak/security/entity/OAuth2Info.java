package com.zb.jogakjogak.security.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "oauth2_info")
public class OAuth2Info {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String provider;

    private String providerId;

    private String accessToken;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;
}

