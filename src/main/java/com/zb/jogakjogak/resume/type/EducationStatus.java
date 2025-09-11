package com.zb.jogakjogak.resume.type;

import lombok.Getter;

@Getter
public enum EducationStatus {
    GRADUATED("졸업"),
    EXPECTED_TO_GRADUATE("졸업 예정"),
    ENROLLED("재학"),
    ON_LEAVE("휴학"),
    DROPOUT("퇴학"),
    COMPLETED("수료");

    private final String description;

    EducationStatus(String description) {
        this.description = description;
    }
}
