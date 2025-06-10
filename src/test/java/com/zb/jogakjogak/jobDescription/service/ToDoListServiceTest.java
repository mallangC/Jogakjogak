package com.zb.jogakjogak.jobDescription.service;

import com.github.javafaker.Faker;
import com.zb.jogakjogak.global.exception.JDErrorCode;
import com.zb.jogakjogak.global.exception.JDException;
import com.zb.jogakjogak.jobDescription.domain.requestDto.ToDoListDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListResponseDto;
import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.jobDescription.entity.ToDoList;
import com.zb.jogakjogak.jobDescription.repsitory.JDRepository;
import com.zb.jogakjogak.jobDescription.repsitory.ToDoListRepository;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ToDoListServiceTest {

    @InjectMocks
    private ToDoListService toDoListService;

    @Mock
    private ToDoListRepository toDoListRepository;

    @Mock
    private JDRepository jdRepository;

    private Long jdId;
    private JD mockJd;
    private ToDoListDto toDoListDto;
    private Faker faker;

    @BeforeEach
    void setUp() {
        jdId = 1L;
        faker = new Faker();
        mockJd = JD.builder()
                .title(faker.job().title())
                .jdUrl(faker.internet().url())
                .endedAt(convertToLocalDate(faker.date().future(365, TimeUnit.DAYS)))
                .build();

        toDoListDto = new ToDoListDto(
                ToDoListType.STRUCTURAL_COMPLEMENT_PLAN,
                "테스트 ToDo 제목",
                "테스트 ToDo 설명",
                "테스트 메모",
                false
        );
    }

    private LocalDateTime convertToLocalDate(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate().atStartOfDay();
    }

    @Test
    @DisplayName("ToDoList 성공적으로 생성")
    void createToDoList_success() {
        // given
        when(jdRepository.findById(jdId)).thenReturn(Optional.of(mockJd));
        when(toDoListRepository.save(any(ToDoList.class))).thenAnswer(invocation -> {
            ToDoList originalToDoList = invocation.getArgument(0);
            ToDoList savedToDoList = ToDoList.builder()
                    .type(originalToDoList.getType())
                    .title(originalToDoList.getTitle())
                    .description(originalToDoList.getDescription())
                    .memo(originalToDoList.getMemo())
                    .isDone(originalToDoList.isDone())
                    .jd(originalToDoList.getJd())
                    .build();
            return savedToDoList;
        });

        // When
        ToDoListResponseDto result = toDoListService.createToDoList(jdId, toDoListDto);

        // Then
        verify(jdRepository, times(1)).findById(jdId);
        verify(toDoListRepository, times(1)).save(any(ToDoList.class));

        assertNotNull(result);
        assertEquals(ToDoListType.STRUCTURAL_COMPLEMENT_PLAN, result.getType());
        assertEquals("테스트 ToDo 제목", result.getTitle());
        assertEquals("테스트 ToDo 설명", result.getDescription());
        assertEquals("테스트 메모", result.getMemo());
        assertFalse(result.isDone());
    }

    @Test
    @DisplayName("ToDoList 생성 실패 - JD를 찾을 수 없음")
    void createToDoList_failure_jdNotFound() {
        // Given
        when(jdRepository.findById(jdId)).thenReturn(Optional.empty());

        // When & Then
        JDException exception = assertThrows(JDException.class, () ->
                toDoListService.createToDoList(jdId, toDoListDto)
        );

        assertEquals(JDErrorCode.JD_NOT_FOUND, exception.getErrorCode());
        assertEquals("JD를 찾을 수 없습니다.", exception.getMessage());

        verify(toDoListRepository, never()).save(any(ToDoList.class));
    }
}