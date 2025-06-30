package com.zb.jogakjogak.jobDescription.domain.responseDto;

import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Schema(description = "다중 Todolist 생성/수정 응답 DTO")
@Getter
@Builder
public class ToDoListGetByCategoryResponseDto {
    @Schema(description = "카테고리", example = "STRUCTURAL_COMPLEMENT_PLAN")
    private ToDoListType category;
    @Schema(description = "분석 아이디", example = "1")
    private Long jdId;
    @Schema(description = "todolist 리스트")
    private List<ToDoListResponseDto> toDoLists;

}
