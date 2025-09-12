package com.zb.jogakjogak.resume.type;

import lombok.Getter;

@Getter
public enum EducationLevel {
    HIGH_SCHOOL("고등학교"),
    ASSOCIATE("전문 학사"),
    BACHELOR("학사"),
    MASTER("석사"),
    DOCTORATE("박사");

    private final String description;

    EducationLevel(String description) {
        this.description = description;
    }
}
