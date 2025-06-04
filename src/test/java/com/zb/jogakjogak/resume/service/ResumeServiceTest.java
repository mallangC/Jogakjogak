package com.zb.jogakjogak.resume.service;

import com.zb.jogakjogak.resume.domain.ResumeRequestDto;
import com.zb.jogakjogak.resume.domain.ResumeResponseDto;
import com.zb.jogakjogak.resume.entity.Resume;
import com.zb.jogakjogak.resume.repository.ResumeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThatThrownBy;



@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock
    private ResumeRepository resumeRepository;

    @InjectMocks
    private ResumeService resumeService;

    @DisplayName("이력서 등록 테스트")
    @Test
    void testRegisterResume() {
        //Given
        String testName = "테스트 이력서";
        String testContent = "이것은 테스트 내용입니다.";
        ResumeRequestDto requestDto = ResumeRequestDto.builder()
                .name(testName)
                .content(testContent)
                .build();

        Resume mockResume = Resume.builder()
                .id(1L)
                .name(testName)
                .content(testContent)
                .isBookMark(false)
                .build();

        given(resumeRepository.save(any(Resume.class))).willReturn(mockResume);

        //When
        ResumeResponseDto responseDto = resumeService.register(requestDto);

        //Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getResumeId()).isEqualTo(1L);
        assertThat(responseDto.getName()).isEqualTo(testName);
        assertThat(responseDto.getContent()).isEqualTo(testContent);

        verify(resumeRepository).save(any(Resume.class));
    }

    @DisplayName("이력서 등록 실패 - 필수 값 누락 시")
    @Test
    void testRegisterResumeFailure_MissingRequiredFields() {
        // Given
        ResumeRequestDto requestDto = ResumeRequestDto.builder()
                .name(null) // 이름 누락
                .content("내용은 있습니다.")
                .build();

        // When & Then
        assertThatThrownBy(() -> resumeService.register(requestDto))
                .isInstanceOf(NullPointerException.class);
    }
}