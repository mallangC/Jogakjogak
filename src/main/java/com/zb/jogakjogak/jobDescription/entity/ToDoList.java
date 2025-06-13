package com.zb.jogakjogak.jobDescription.entity;


import com.zb.jogakjogak.global.BaseEntity;
import com.zb.jogakjogak.jobDescription.domain.requestDto.ToDoListDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.ToDoListUpdateRequestDto;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToDoList extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ToDoListType category;

    @Builder.Default
    @Column(nullable = false)
    private boolean isDone = false;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @Column(columnDefinition = "VARCHAR(255) DEFAULT ''")
    private String memo = "";


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jd_id", nullable = false)
    private JD jd;

    public static ToDoList fromDto(ToDoListDto dto, JD jd) {

        String escapedContent = dto.getContent().replace("'", "''");

        return ToDoList.builder()
                .category(dto.getCategory())
                .title(dto.getTitle())
                .content(escapedContent)
                .memo(dto.getMemo())
                .isDone(dto.isDone())
                .jd(jd)
                .build();
    }

    public static ToDoList fromDto(ToDoListUpdateRequestDto dto, JD jd) {

        return ToDoList.builder()
                .category(dto.getCategory())
                .title(dto.getTitle())
                .content(dto.getContent())
                .memo(dto.getMemo())
                .isDone(dto.isDone())
                .jd(jd)
                .build();
    }
    public void setJd(JD jd) {
        this.jd = jd;
    }

    public void updateFromDto(ToDoListDto dto) {
        this.category = dto.getCategory();
        this.title = dto.getTitle();
        this.content = dto.getContent();
        this.memo = dto.getMemo() != null ? dto.getMemo() : "";
        this.isDone = dto.isDone();
    }
}
