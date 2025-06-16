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
    private List<MessageDto> messages;
    private Double temperature;
    @JsonProperty("max_tokens")
    private Integer maxTokens;
}
