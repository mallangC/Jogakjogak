package com.zb.jogakjogak.jobDescription.domain.requestDto;

import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToDoListDto {

    private ToDoListType category;
    private String title;
    private String content;
    private String memo;
    private boolean isDone;
}
