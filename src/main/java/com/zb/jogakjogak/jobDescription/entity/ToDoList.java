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

    @Builder
    private ToDoList(ToDoListType type, String title, String description) {
        this.type = type;
        this.title = title;
        this.description = description;
    }

    public static ToDoList fromDto(ToDoListDto dto) {
        return ToDoList.builder()
                .type(dto.getType())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .build();
    }
}
