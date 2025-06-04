package com.zb.jogakjogak.resume.entity;

import com.zb.jogakjogak.global.BaseEntity;
import com.zb.jogakjogak.resume.domain.ResumeRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Resume extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 30)
    private String name;
    @Column(nullable = false, length = 5000)
    private String content;
    private boolean isBookMark;

    /**
     * 사용자가 이력서를 수정할 때 사용하는 메서드
     * @param requestDto 수정할 이력서 이름, 수정할 이력서 내용
     */
    public void modify(ResumeRequestDto requestDto) {
        this.name = requestDto.getName();
        this.content = requestDto.getContent();
    }
}
