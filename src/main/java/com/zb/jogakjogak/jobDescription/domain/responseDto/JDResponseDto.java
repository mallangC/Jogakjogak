package com.zb.jogakjogak.jobDescription.domain.responseDto;

import com.zb.jogakjogak.jobDescription.entity.JD;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class JDResponseDto {
    private Long jd_id;
    private String title;
    private String companyName;
    private String job;
    private String content;
    private String jdUrl;
    private String memo;
    private Long memberId;
    private boolean isAlarmOn;
    private LocalDateTime applyAt;
    private LocalDate endedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<ToDoListResponseDto> toDoLists;

    public static JDResponseDto fromEntity(JD jd) {
        List<ToDoListResponseDto> mappedToDoLists = null;
        if (jd.getToDoLists() != null && Hibernate.isInitialized(jd.getToDoLists())) {
            mappedToDoLists = jd.getToDoLists().stream()
                    .map(ToDoListResponseDto::fromEntity)
                    .collect(Collectors.toList());
        }

        return JDResponseDto.builder()
                .jd_id(jd.getId())
                .title(jd.getTitle())
                .companyName(jd.getCompanyName())
                .job(jd.getJob())
                .content(jd.getContent())
                .jdUrl(jd.getJdUrl())
                .memo(jd.getMemo())
                .isAlarmOn(jd.isAlarmOn())
                .applyAt(jd.getApplyAt())
                .endedAt(jd.getEndedAt())
                .createdAt(jd.getCreatedAt())
                .updatedAt(jd.getUpdatedAt())
                .toDoLists(mappedToDoLists)
                .memberId(jd.getMember().getId())
                .build();
    }
}
