package com.zb.jogakjogak.jobDescription.domain.responseDto;

import com.zb.jogakjogak.jobDescription.entity.ToDoList;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "Todolist 단건 응답 DTO")
@Getter
@Builder
public class ToDoListResponseDto {
    @Schema(description = "todolist 아이디", example = "1")
    private Long checklist_id;
    @Schema(description = "카테고리", example = "STRUCTURAL_COMPLEMENT_PLAN")
    private ToDoListType category;
    @Schema(description = "todolist 제목", example = "NoSQL 학습")
    private String title;
    @Schema(description = "todolist 내용", example = "유튜브에 있는 NoSQL에 관련된 강의 듣기")
    private String content;
    @Schema(description = "todolist 메모", example = "추후 개발 예정")
    private String memo;
    @Schema(description = "todolist 완료 여부", example = "true")
    private boolean isDone;
    @Schema(description = "분석 아이디", example = "1")
    private Long jdId;
    @Schema(description = "todolist 생성 일시", example = "2025-06-22T10:30:00Z")
    private LocalDateTime createdAt;
    @Schema(description = "todolist 수정 일시", example = "2025-06-22T10:30:00Z")
    private LocalDateTime updatedAt;

    public static ToDoListResponseDto fromEntity(ToDoList toDoList) {
        return ToDoListResponseDto.builder()
                .checklist_id(toDoList.getId())
                .category(toDoList.getCategory())
                .title(toDoList.getTitle())
                .content(toDoList.getContent())
                .memo(toDoList.getMemo())
                .isDone(toDoList.isDone())
                .jdId(toDoList.getJd().getId())
                .createdAt(toDoList.getCreatedAt())
                .updatedAt(toDoList.getUpdatedAt())
                .build();
    }
}
