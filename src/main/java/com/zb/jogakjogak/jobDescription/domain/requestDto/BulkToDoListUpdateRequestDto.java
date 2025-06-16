package com.zb.jogakjogak.jobDescription.domain.requestDto;

import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkToDoListUpdateRequestDto {
    private ToDoListType category;
    private List<ToDoListUpdateRequestDto> updatedOrCreateToDoLists;
    private List<Long> deletedToDoListIds;
}
