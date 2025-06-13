package com.zb.jogakjogak.jobDescription.domain.requestDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import lombok.Getter;

@Getter
public class ToDoListUpdateRequestDto {
    @JsonProperty("checklist_id")
    private Long id;
    private String title;
    private ToDoListType category;
    private String content;
    private String memo;
    private Long jdId;
    @JsonProperty("done")
    private boolean isDone;
}
