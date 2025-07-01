package com.zb.jogakjogak.jobDescription.domain.responseDto;

import com.zb.jogakjogak.resume.domain.responseDto.ResumeResponseDto;
import com.zb.jogakjogak.resume.entity.Resume;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "분석 전체 페이징 응답 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagedJdResponseDto {
    @Schema(description = "이력서")
    private ResumeResponseDto resume;
    @Schema(description = "분석 전체 리스트")
    private List<AllGetJDResponseDto> jds;
    @Schema(description = "전체 페이지 수", example = "10")
    private int totalPages;
    @Schema(description = "전체 요소 개수", example = "150")
    private long totalElements;
    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    private int currentPage;
    @Schema(description = "페이지당 요소 개수", example = "10")
    private int pageSize;
    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private boolean hasNext;
    @Schema(description = "이전 페이지 존재 여부", example = "false")
    private boolean hasPrevious;
    @Schema(description = "현재 페이지가 첫 번째 페이지인지 여부", example = "true")
    private boolean isFirst;
    @Schema(description = "현재 페이지가 마지막 페이지인지 여부", example = "false")
    private boolean isLast;

    public PagedJdResponseDto(Page<AllGetJDResponseDto> page, Resume resume) {
        this.jds = page.getContent();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.currentPage = page.getNumber();
        this.pageSize = page.getSize();
        this.hasNext = page.hasNext();
        this.hasPrevious = page.hasPrevious();
        this.isFirst = page.isFirst();
        this.isLast = page.isLast();
        this.resume = (resume != null) ? new ResumeResponseDto(resume) : null;
    }
}
