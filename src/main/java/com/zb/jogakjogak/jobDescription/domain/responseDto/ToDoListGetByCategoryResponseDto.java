package com.zb.jogakjogak.jobDescription.domain.responseDto;

import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ToDoListGetByCategoryResponseDto {

    private ToDoListType category;
    private Long jdId;
    private List<ToDoListResponseDto> responseDtoList;

}
