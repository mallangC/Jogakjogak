package com.zb.jogakjogak.jobDescription.domain.requestDto;

import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "todolist 생성,수정,삭제 요청 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkToDoListUpdateRequestDto {
    @Schema(description = "todolist 카태고라", example = "STRUCTURAL_COMPLEMENT_PLAN", requiredMode = Schema.RequiredMode.REQUIRED)
    private ToDoListType category;
    @Schema(description = "생성/수정할 todolist")
    private List<ToDoListUpdateRequestDto> updatedOrCreateToDoLists;
    @Schema(description = "삭제할 todolist 아이디")
    private List<Long> deletedToDoListIds;
}
