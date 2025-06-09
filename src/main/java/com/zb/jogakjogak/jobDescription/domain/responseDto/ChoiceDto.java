package com.zb.jogakjogak.jobDescription.domain.responseDto;

import com.zb.jogakjogak.jobDescription.domain.requestDto.MessageDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChoiceDto {
    private Integer index;
    private MessageDto message;
}
