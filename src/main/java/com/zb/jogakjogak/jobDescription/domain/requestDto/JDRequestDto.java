package com.zb.jogakjogak.jobDescription.domain.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JDRequestDto {
    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(max = 30, message = "제목의 최대 길이는 30자입니다.")
    private String title;
    @NotBlank(message = "채용 공고 URL은 필수 입력 항목입니다.")
    @URL(message = "유효한 URL 형식이 아닙니다.")
    private String jdUrl;
    @NotBlank(message = "회사 이름은 필수 항목입니다.")
    private String companyName;
    @NotBlank(message = "직무 이름은 필수 항목입니다.")
    private String job;
    @NotBlank(message = "채용 공고는 필수 항목입니다.")
    private String content;
    @NotNull(message = "마감일은 필수 항목입니다.")
    private LocalDateTime endedAt;
}
