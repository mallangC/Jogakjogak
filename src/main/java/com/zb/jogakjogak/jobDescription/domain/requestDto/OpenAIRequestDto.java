package com.zb.jogakjogak.jobDescription.domain.requestDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIRequestDto {
    private String model;
    private List<MessageDto> messages; // MessageDto 리스트
    private Double temperature;
    @JsonProperty("max_tokens") // JSON 필드 이름 매핑
    private Integer maxTokens;
    // JSON 응답 형식을 강제하려면 (모델이 지원하는 경우)
    // @JsonProperty("response_format")
    // private ResponseFormatDto responseFormat;
}
