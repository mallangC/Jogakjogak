package com.zb.jogakjogak.notification.dto;

import com.zb.jogakjogak.jobDescription.entity.JD;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


@Data
@Builder
public class JdEmailDto {
    private String title;
    private String companyName;
    private Long expiredDay;
    private Integer completedTodoLists;
    private Integer notCompletedTodoLists;
    private Integer totalTodoLists;


    /**
     * JD 엔티티에서 이메일용 데이터로 변환하는 메서드
     */
    public static JdEmailDto from(JD jd, Integer completedTodoLists, Integer totalTodoLists) {
        Integer notCompletedTodoLists = totalTodoLists - completedTodoLists;
        Long expiredDay = ChronoUnit.DAYS.between(
                LocalDateTime.now().toLocalDate(),
                jd.getEndedAt().toLocalDate()
        );

        return JdEmailDto.builder()
                .title(jd.getTitle())
                .companyName(jd.getCompanyName())
                .expiredDay(expiredDay)
                .completedTodoLists(completedTodoLists)
                .notCompletedTodoLists(notCompletedTodoLists)
                .totalTodoLists(totalTodoLists)
                .build();
    }
}