package com.zb.jogakjogak.jobDescription.domain.requestDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "다중 Todolist 생성/수정 요청 DTO")
@Getter
@Builder
public class ToDoListUpdateRequestDto {
    @Schema(description = "todolist 아이디", example = "1")
    @JsonProperty("checklist_id")
    private Long id;
    @Schema(description = "todolist 제목", example = "NoSQL 학습", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;
    @Schema(description = "todolist 내용", example = "유튜브에 있는 NoSQL에 관련된 강의 듣기", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
    @Schema(description = "todolist 완료 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("done")
    private boolean isDone;
}
