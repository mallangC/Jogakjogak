package com.zb.jogakjogak.jobDescription.domain.requestDto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkRequestDto {
    private boolean isBookmark;
}
