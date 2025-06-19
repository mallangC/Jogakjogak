package com.zb.jogakjogak.security.entity;

import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.notification.entity.Notification;
import com.zb.jogakjogak.resume.entity.Resume;
import com.zb.jogakjogak.security.Role;
import com.zb.jogakjogak.security.dto.KakaoResponseDto;
import com.zb.jogakjogak.security.dto.OAuth2ResponseDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Member{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank

    private String username;


    @Email
    private String email;

    private String password;

    private String name;

    private String nickname;


    private String phoneNumber;

    private Role role;

    private LocalDateTime registeredAt;

    private LocalDateTime lastLoginAt;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OAuth2Info> oauth2Info = new ArrayList<>();

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Resume resume;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<JD> jdList = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Notification> notification;

    @PrePersist
    public void prePersist(){
        this.registeredAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate(){
        this.lastLoginAt = LocalDateTime.now();
    }

    public void updateExistingMember(OAuth2ResponseDto oAuth2ResponseDto){
        this.email = oAuth2ResponseDto.getEmail();
        this.nickname = oAuth2ResponseDto.getNickname();
        this.phoneNumber = oAuth2ResponseDto.getPhoneNumber();
        this.name = oAuth2ResponseDto.getName();
    }
}
