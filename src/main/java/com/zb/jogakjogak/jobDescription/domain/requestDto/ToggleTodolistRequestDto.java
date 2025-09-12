package com.zb.jogakjogak.jobDescription.domain.requestDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "투두리스트 완료 상태 요청 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToggleTodolistRequestDto {
    @Schema(description = "todolist 완료 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("is_done")
    private boolean isDone;
}
