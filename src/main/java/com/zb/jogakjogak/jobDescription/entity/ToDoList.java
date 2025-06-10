package com.zb.jogakjogak.jobDescription.entity;

import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListDto;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
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

    @Column(columnDefinition = "VARCHAR(255) DEFAULT ''")
    private String memo = "";

    @Column(nullable = false)
    private boolean isDone = false;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jd_id", nullable = false)
    private JD jd;

    @Builder
    private ToDoList(ToDoListType type, String title, String description, String memo, Boolean isDone, JD jd) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.memo = (memo != null) ? memo : this.memo;
        this.isDone = isDone != null ? isDone : false;
        this.jd = jd;
    }

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

}
