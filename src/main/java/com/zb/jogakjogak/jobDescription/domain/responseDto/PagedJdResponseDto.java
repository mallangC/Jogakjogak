package com.zb.jogakjogak.jobDescription.domain.responseDto;

import com.zb.jogakjogak.resume.domain.responseDto.ResumeResponseDto;
import com.zb.jogakjogak.security.entity.Member;
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
    @Schema(description = "등록된 전체 채용공고 갯수", example = "3")
    private int postedJdCount;
    @Schema(description = "지원 완료된 채용공고 갯수", example = "2")
    private int applyJdCount;
    @Schema(description = "모든 채용공고의 완료된 조각 갯수", example = "10")
    private int completedPiecesCount;
    @Schema(description = "모든 채용공고의 조각 갯수", example = "100")
    private int totalPiecesCount;
    @Schema(description = "모든 조각을 완료한 채용공고 갯수", example = "1")
    private int perfectJdCount;
    @Schema(description = "첫 접속 유무", example = "true")
    private Boolean isOnboarded;

    public PagedJdResponseDto(Page<AllGetJDResponseDto> page, Member member, int postedJdCount, int applyCount, int completedPiecesCount, int totalPiecesCount, int perfectJdCount) {
        this.jds = page.getContent();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.currentPage = page.getNumber();
        this.pageSize = page.getSize();
        this.hasNext = page.hasNext();
        this.hasPrevious = page.hasPrevious();
        this.isFirst = page.isFirst();
        this.isLast = page.isLast();
        this.resume = (member.getResume() != null) ? new ResumeResponseDto(member.getResume()) : null;
        this.postedJdCount = postedJdCount;
        this.applyJdCount = applyCount;
        this.completedPiecesCount = completedPiecesCount;
        this.totalPiecesCount = totalPiecesCount;
        this.perfectJdCount = perfectJdCount;
        this.isOnboarded = member.isOnboarded();
    }
}
