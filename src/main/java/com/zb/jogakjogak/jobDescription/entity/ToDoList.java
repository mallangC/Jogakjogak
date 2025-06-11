package com.zb.jogakjogak.jobDescription.entity;


import com.zb.jogakjogak.jobDescription.domain.requestDto.ToDoListDto;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToDoList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ToDoListType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(columnDefinition = "VARCHAR(255) DEFAULT ''")
    private String memo = "";

    @Builder.Default
    @Column(nullable = false)
    private boolean isDone = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jd_id", nullable = false)
    private JD jd;

    public static ToDoList fromDto(ToDoListDto dto, JD jd) {
        return ToDoList.builder()
                .type(dto.getType())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .memo(dto.getMemo())
                .isDone(dto.isDone())
                .jd(jd)
                .build();
    }
    public void setJd(JD jd) {
        this.jd = jd;
    }

    public void updateFromDto(ToDoListDto dto) {
        this.type = dto.getType();
        this.title = dto.getTitle();
        this.description = dto.getDescription();
        this.memo = dto.getMemo() != null ? dto.getMemo() : ""; // memo는 null 방지
        this.isDone = dto.isDone();
    }
}
