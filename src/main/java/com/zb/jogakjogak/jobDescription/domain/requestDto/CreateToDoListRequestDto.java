package com.zb.jogakjogak.jobDescription.domain.requestDto;

import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "Todolist 생성 요청 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateToDoListRequestDto {
    @Schema(description = "todolist 카테고리", example = "STRUCTURAL_COMPLEMENT_PLAN", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "카테고리는 필수 입력 항목입니다.")
    private ToDoListType category;
    @Schema(description = "todolist 제목", example = "NoSQL 학습", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(max = 50, message = "제목의 최대 길이는 50자입니다.")
    private String title;
    @Schema(description = "todolist 내용", example = "유튜브에 있는 NoSQL에 관련된 강의 듣기", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "상세 설명은 필수 입력 항목입니다.")
    private String content;
}
