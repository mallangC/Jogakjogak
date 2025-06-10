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

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jd_id", nullable = false)
    private JD jd;

    @Builder
    private ToDoList(ToDoListType type, String title, String description, JD jd) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.jd = jd;
    }

    public static ToDoList fromDto(ToDoListDto dto, JD jd) {
        return ToDoList.builder()
                .type(dto.getType())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .jd(jd)
                .build();
    }

}
