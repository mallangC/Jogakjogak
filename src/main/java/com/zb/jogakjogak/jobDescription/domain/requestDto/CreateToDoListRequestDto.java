package com.zb.jogakjogak.jobDescription.domain.requestDto;

import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateToDoListRequestDto {

    @NotNull(message = "카테고리는 필수 입력 항목입니다.")
    private ToDoListType category;
    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(max = 15, message = "제목의 최대 길이는 30자입니다.")
    private String title;
    @NotBlank(message = "상세 설명은 필수 입력 항목입니다.")
    private String content;
}
