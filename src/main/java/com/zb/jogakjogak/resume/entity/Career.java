package com.zb.jogakjogak.resume.entity;

import com.zb.jogakjogak.resume.domain.requestDto.CareerDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Career {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDate joinedAt;
    private LocalDate quitAt;
    @Column(nullable = false)
    private boolean isWorking;
    @Column(nullable = false)
    private String companyName;
    @Column(nullable = false)
    private String workPerformance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    public static Career of(CareerDto careerDto, Resume resume) {
        return Career.builder()
                .joinedAt(careerDto.getJoinedAt())
                .quitAt(careerDto.getQuitAt())
                .isWorking(careerDto.isWorking())
                .companyName(careerDto.getCompanyName())
                .workPerformance(careerDto.getWorkPerformance())
                .resume(resume)
                .build();
    }

}
