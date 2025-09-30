package com.zb.jogakjogak.event.entity;

import com.zb.jogakjogak.event.type.EventType;
import com.zb.jogakjogak.security.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String code;
    @Enumerated(EnumType.STRING)
    private EventType type;
    @Column(nullable = false)
    private Boolean isFirst = true;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public void notFirst(){
        this.isFirst = false;
    }
}
