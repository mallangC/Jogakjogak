package com.zb.jogakjogak.jobDescription.domain.requestDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ToDoListUpdateRequestDto {
    @JsonProperty("checklist_id")
    private Long id;
    private String title;
    private String content;
    @JsonProperty("done")
    private boolean isDone;
}
