package com.zb.jogakjogak.jobDescription.entity;


import com.zb.jogakjogak.global.BaseEntity;
import com.zb.jogakjogak.jobDescription.domain.requestDto.ToDoListDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.ToDoListUpdateRequestDto;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import jakarta.persistence.*;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToDoList extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ToDoListType category;

    @Builder.Default
    @Column(nullable = false)
    private boolean isDone = false;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @Column(columnDefinition = "VARCHAR(255) DEFAULT ''")
    private String memo = "";


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jd_id", nullable = false)
    private JD jd;

    public static ToDoList fromDto(ToDoListDto dto, JD jd) {

        Logger logger = LoggerFactory.getLogger(ToDoList.class);

        String escapedContent = dto.getContent().replace("'", "''");

        ToDoListType category = dto.getCategory();
        if (category == null) {
            logger.warn("LLM 응답에서 ToDoList category가 누락되었습니다. 기본값으로 설정합니다.");
            category = ToDoListType.STRUCTURAL_COMPLEMENT_PLAN;
        }

        String title = dto.getTitle();
        if (title == null || title.isEmpty()) {
            logger.warn("LLM 응답에서 ToDoList title이 누락되었습니다. 기본값으로 설정합니다.");
            title = "제목 없음";
        }

        if (escapedContent.isEmpty()) {
            logger.warn("LLM 응답에서 ToDoList content가 누락되었습니다. 기본값으로 설정합니다.");
            escapedContent = "내용 없음";
        }
        return ToDoList.builder()
                .category(category)
                .title(title)
                .content(escapedContent)
                .memo(dto.getMemo())
                .isDone(dto.isDone())
                .jd(jd)
                .build();
    }

    public static ToDoList fromDto(ToDoListUpdateRequestDto dto, JD jd) {

        return ToDoList.builder()
                .category(dto.getCategory())
                .title(dto.getTitle())
                .content(dto.getContent())
                .memo(dto.getMemo())
                .isDone(dto.isDone())
                .jd(jd)
                .build();
    }
    public void setJd(JD jd) {
        this.jd = jd;
    }

    public void updateFromDto(ToDoListDto dto) {
        this.category = dto.getCategory();
        this.title = dto.getTitle();
        this.content = dto.getContent();
        this.memo = dto.getMemo() != null ? dto.getMemo() : "";
        this.isDone = dto.isDone();
    }

    public void updateFromBulkUpdateToDoLists(ToDoListUpdateRequestDto dto) {
        this.category = dto.getCategory();
        this.title = dto.getTitle();
        this.content = dto.getContent();
        this.memo = dto.getMemo() != null ? dto.getMemo() : "";
        this.isDone = dto.isDone();
    }
}
