package com.zb.jogakjogak.jobDescription.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ToDoListType {

    STRUCTURAL_COMPLEMENT_PLAN("구조적 보완 계획"),
    CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL("내용 강조/재구성 제안(표현 및 피드백 기반)"),
    SCHEDULE_MISC_ERROR("일정 관리 및 기타");

    private final String koreanDescription;

    public String getKoreanDescription() {
        return koreanDescription;
    }
}
