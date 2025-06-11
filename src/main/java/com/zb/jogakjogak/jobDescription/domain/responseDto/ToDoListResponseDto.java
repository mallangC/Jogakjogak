package com.zb.jogakjogak.jobDescription.domain.responseDto;

import com.zb.jogakjogak.jobDescription.entity.ToDoList;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ToDoListResponseDto {
    private Long checklist_id;
    private ToDoListType type;
    private String title;
    private String description;
    private String memo;
    private boolean isDone;
    private Long jdId;

    public static ToDoListResponseDto fromEntity(ToDoList toDoList) {
        return ToDoListResponseDto.builder()
                .checklist_id(toDoList.getId())
                .type(toDoList.getCategory())
                .title(toDoList.getTitle())
                .description(toDoList.getContent())
                .memo(toDoList.getMemo())
                .isDone(toDoList.isDone())
                .jdId(toDoList.getJd() != null ? toDoList.getJd().getId() : null)
                .build();
    }
}
