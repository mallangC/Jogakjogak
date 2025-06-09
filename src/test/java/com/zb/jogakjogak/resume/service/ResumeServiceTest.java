package com.zb.jogakjogak.resume.service;


import com.github.javafaker.Faker;
import com.zb.jogakjogak.global.exception.ResumeErrorCode;
import com.zb.jogakjogak.global.exception.ResumeException;
import com.zb.jogakjogak.resume.domain.requestDto.ResumeRequestDto;
import com.zb.jogakjogak.resume.domain.responseDto.ResumeDeleteResponseDto;
import com.zb.jogakjogak.resume.domain.responseDto.ResumeResponseDto;
import com.zb.jogakjogak.resume.entity.Resume;
import com.zb.jogakjogak.resume.repository.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock
    private ResumeRepository resumeRepository;

    @InjectMocks
    private ResumeService resumeService;

    private Resume sampleResume;
    private ResumeRequestDto sampleRequestDto;
    private Faker faker;



    @BeforeEach
    void setUp() {
        faker = new Faker();

        sampleResume = Resume.builder()
                .id(1L)
                .name(faker.job().title())
                .content("기존 내용")
                .isBookMark(false)
                .build();

        sampleRequestDto = ResumeRequestDto.builder()
                .name("새로운 이름")
                .content("새로운 내용")
                .build();
    }

    @DisplayName("이력서 등록 테스트")
    @Test
    void createResume_success() {
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

        verify(resumeRepository, times(1)).save(any(Resume.class));
    }


    @Test
    @DisplayName("이력서 수정 성공 테스트 - 200 OK 예상")
    void modify_success() {
        //Given
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(sampleResume));

        //When
        ResumeResponseDto result = resumeService.modify(1L, sampleRequestDto);

        //Then
        verify(resumeRepository, times(1)).findById(1L);

        assertEquals(sampleRequestDto.getName(), sampleResume.getName());
        assertEquals(sampleRequestDto.getContent(), sampleResume.getContent());

        assertNotNull(result);
        assertEquals(sampleResume.getId(), result.getResumeId());
        assertEquals(sampleRequestDto.getName(), result.getName());
        assertEquals(sampleRequestDto.getContent(), result.getContent());
    }

    @Test
    @DisplayName("이력서 수정 실패 테스트 - 이력서를 찾을 수 없음")
    void modify_fail_notFoundResume() {
        //Given
        Long nonExistentResumeId = 99L;
        when(resumeRepository.findById(nonExistentResumeId)).thenReturn(Optional.empty());

        // When & Then
        ResumeException exception = assertThrows(ResumeException.class, () -> {
            resumeService.modify(nonExistentResumeId, sampleRequestDto);
        });

        // 예외 메시지 또는 에러 코드 검증
        assertEquals(ResumeErrorCode.NOT_FOUND_RESUME, exception.getErrorCode());

        // findById 메소드가 호출되었는지 확인
        verify(resumeRepository, times(1)).findById(nonExistentResumeId);
    }

    @Test
    @DisplayName("이력서 조회 성공 테스트 - 200 OK 예상")
    void get_success() {
        //Given
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(sampleResume));

        //When
        ResumeResponseDto result = resumeService.get(1L);

        //Then
        verify(resumeRepository, times(1)).findById(1L);

        assertNotNull(result);
        assertEquals(sampleResume.getId(), result.getResumeId());
        assertEquals(sampleResume.getName(), result.getName());
        assertEquals(sampleResume.getContent(), result.getContent());
    }

    @Test
    @DisplayName("이력서 조회 실패 테스트 - 이력서를 찾을 수 없음")
    void get_fail_notFoundResume() {
        //Given
        Long nonExistentResumeId = 99L;
        when(resumeRepository.findById(nonExistentResumeId)).thenReturn(Optional.empty());

        // When & Then
        ResumeException exception = assertThrows(ResumeException.class, () -> {
            resumeService.get(nonExistentResumeId);
        });

        // 예외 메시지 또는 에러 코드 검증
        assertEquals(ResumeErrorCode.NOT_FOUND_RESUME, exception.getErrorCode());

        // findById 메소드가 호출되었는지 확인
        verify(resumeRepository, times(1)).findById(nonExistentResumeId);
    }

    @DisplayName("이력서 삭제 성공 테스트")
    @Test
    void deleteResume_success() {
        // Given
        Long resumeIdToDelete = 1L;
        given(resumeRepository.findById(resumeIdToDelete)).willReturn(Optional.of(sampleResume));
        willDoNothing().given(resumeRepository).delete(sampleResume);

        // When
        ResumeDeleteResponseDto responseDto = resumeService.delete(resumeIdToDelete);

        // Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getResumeId()).isEqualTo(resumeIdToDelete);

        verify(resumeRepository, times(1)).findById(resumeIdToDelete);
        verify(resumeRepository, times(1)).delete(sampleResume);
    }

    @DisplayName("이력서 삭제 실패 테스트 - 이력서를 찾을 수 없음")
    @Test
    void deleteResume_fail_notFound() {
        // Given
        Long nonExistentResumeId = 99L;
        given(resumeRepository.findById(nonExistentResumeId)).willReturn(Optional.empty());

        // When & Then
        ResumeException exception = assertThrows(ResumeException.class, () -> {
            resumeService.delete(nonExistentResumeId);
        });

        assertEquals(ResumeErrorCode.NOT_FOUND_RESUME, exception.getErrorCode());

        verify(resumeRepository, times(1)).findById(nonExistentResumeId);
        verify(resumeRepository, times(0)).delete(any(Resume.class));
    }

}
