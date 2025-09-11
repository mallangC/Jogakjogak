package com.zb.jogakjogak.jobDescription.domain.requestDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Schema(description = "todolist 완료여부 수정 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTodoListsIsDoneRequestDto {
    @Schema(description = "수정할 todolist id")
    private List<Long> toDoListIds;
    @Schema(description = "일괄처리할 완료여부", example = "true")
    private boolean isDone;
}
