package com.zb.jogakjogak.resume.entity;

import com.zb.jogakjogak.resume.domain.requestDto.EducationDto;
import com.zb.jogakjogak.resume.type.EducationLevel;
import com.zb.jogakjogak.resume.type.EducationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Education {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private EducationLevel level;
    @Column(nullable = false)
    private String majorField;
    @Column(nullable = false)
    private EducationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    public static Education of(EducationDto dto, Resume resume){
        return Education.builder()
                .level(dto.getLevel())
                .majorField(dto.getMajorField())
                .status(dto.getStatus())
                .resume(resume)
                .build();
    }
}
