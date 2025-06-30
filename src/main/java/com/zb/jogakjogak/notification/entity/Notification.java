package com.zb.jogakjogak.notification.entity;


import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.security.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JD> jdList = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

}
