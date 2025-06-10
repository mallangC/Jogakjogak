package com.zb.jogakjogak.jobDescription.domain.responseDto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ToDoListDto {
    private ToDoListType type;
    private String title;
    private String description;
    private String memo;
    private boolean isDone;

    @JsonCreator
    public ToDoListDto(
            @JsonProperty("type") ToDoListType type,
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("memo") String memo,
            @JsonProperty("isDone") boolean isDone
    ) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.memo = memo;
        this.isDone = isDone;
    }

    public ToDoListDto(ToDoListType type, String title, String description) {
        this(type, title, description, "", false);
    }
}
