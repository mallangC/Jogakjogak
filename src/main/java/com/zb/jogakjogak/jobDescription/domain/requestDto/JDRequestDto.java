package com.zb.jogakjogak.jobDescription.domain.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

@Getter
@Builder
public class JDRequestDto {
    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(max = 255, message = "제목의 최대 길이는 255자입니다.")
    private String title;
    @NotBlank(message = "채용 공고 URL은 필수 입력 항목입니다.")
    @URL(message = "유효한 URL 형식이 아닙니다.")
    private String JDUrl;
    //TODO: 이력서 사용자 입력을 받을 필드 작성
    @NotNull(message = "마감일은 필수 선택 항목입니다.")
    private LocalDateTime endedAt;
}
