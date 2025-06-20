package com.zb.jogakjogak.jobDescription.domain.responseDto;

import com.zb.jogakjogak.jobDescription.entity.ToDoList;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ToDoListResponseDto {
    private Long checklist_id;
    private ToDoListType category;
    private String title;
    private String content;
    private String memo;
    private boolean isDone;
    private Long jdId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ToDoListResponseDto fromEntity(ToDoList toDoList) {
        return ToDoListResponseDto.builder()
                .checklist_id(toDoList.getId())
                .category(toDoList.getCategory())
                .title(toDoList.getTitle())
                .content(toDoList.getContent())
                .memo(toDoList.getMemo())
                .isDone(toDoList.isDone())
                .jdId(toDoList.getJd().getId())
                .createdAt(toDoList.getCreatedAt())
                .updatedAt(toDoList.getUpdatedAt())
                .build();
    }
}
