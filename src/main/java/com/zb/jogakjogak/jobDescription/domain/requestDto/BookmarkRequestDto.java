package com.zb.jogakjogak.jobDescription.domain.requestDto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkRequestDto {
    @JsonProperty("isBookmark")
    private boolean isBookmark;
}
