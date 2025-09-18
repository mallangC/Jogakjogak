package com.zb.jogakjogak.resume.domain.requestDto;

import com.zb.jogakjogak.resume.entity.Career;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CareerDto {
    @NotBlank(message = "입사년월을 입력해주세요.")
    private LocalDate joinedAt;
    private LocalDate quitAt;
    @NotBlank(message = "재직 여부를 입력해주세요.")
    private Boolean isWorking;
    @NotBlank(message = "회사 이름을 입력해주세요.")
    private String companyName;
    @NotBlank(message = "담당 업무와 주요 성과를 입력해주세요.")
    private String workPerformance;

    public static CareerDto of(Career career) {
        return CareerDto.builder()
                .joinedAt(career.getJoinedAt())
                .quitAt(career.getQuitAt())
                .isWorking(career.getIsWorking())
                .companyName(career.getCompanyName())
                .workPerformance(career.getWorkPerformance())
                .build();
    }
}
