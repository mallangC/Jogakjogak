package com.zb.jogakjogak.jobDescription.domain.responseDto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "todolist 완료여부 수정 DTO")
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UpdateIsDoneTodoListsResponseDto {

    @Schema(description = "수정된 todolist id")
    private List<ToDoListResponseDto> toDoLists;
    @Schema(description = "일괄처리된 완료여부", example = "true")
    private boolean isDone;
}
