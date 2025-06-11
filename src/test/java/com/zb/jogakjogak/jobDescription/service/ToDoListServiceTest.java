package com.zb.jogakjogak.jobDescription.service;

import com.github.javafaker.Faker;
import com.zb.jogakjogak.global.exception.JDErrorCode;
import com.zb.jogakjogak.global.exception.JDException;
import com.zb.jogakjogak.global.exception.ToDoListErrorCode;
import com.zb.jogakjogak.global.exception.ToDoListException;
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

import java.time.LocalDate;
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
    private Long toDoListId;
    private ToDoList mockToDoList;
    private ToDoListDto createToDoListDto;
    private ToDoListDto updateToDoListDto;

    @BeforeEach
    void setUp() {
        jdId = 1L;
        toDoListId = 101L;
        Faker faker = new Faker();

        mockJd = JD.builder()
                .id(jdId)
                .title(faker.job().title())
                .jdUrl(faker.internet().url())
                .endedAt(faker.date().future(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .build();

        createToDoListDto = new ToDoListDto(
                faker.options().option(ToDoListType.class),
                faker.lorem().sentence(3, 5),
                faker.lorem().paragraph(2),
                faker.lorem().sentence(1),
                faker.bool().bool()
        );

        updateToDoListDto = new ToDoListDto(
                faker.options().option(ToDoListType.class),
                faker.lorem().sentence(4, 6),
                faker.lorem().paragraph(3),
                faker.lorem().sentence(2),
                !createToDoListDto.isDone()
        );

        mockToDoList = ToDoList.builder()
                .id(toDoListId)
                .category(createToDoListDto.getType())
                .title(createToDoListDto.getTitle())
                .content(createToDoListDto.getDescription())
                .memo(createToDoListDto.getMemo())
                .isDone(createToDoListDto.isDone())
                .jd(mockJd)
                .build();
    }

    private LocalDateTime convertToLocalDate(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate().atStartOfDay();
    }

    @Test
    @DisplayName("ToDoList 성공적으로 생성")
    void createToDoList_success() {
        // Given
        when(jdRepository.findById(jdId)).thenReturn(Optional.of(mockJd));
        when(toDoListRepository.save(any(ToDoList.class))).thenAnswer(invocation -> {
            ToDoList originalToDoList = invocation.getArgument(0);
            return ToDoList.builder()
                    .category(originalToDoList.getCategory())
                    .title(originalToDoList.getTitle())
                    .content(originalToDoList.getContent())
                    .memo(originalToDoList.getMemo())
                    .isDone(originalToDoList.isDone())
                    .jd(originalToDoList.getJd())
                    .build();
        });

        // When
        ToDoListResponseDto result = toDoListService.createToDoList(jdId, createToDoListDto);

        // Then
        verify(jdRepository, times(1)).findById(jdId);
        verify(toDoListRepository, times(1)).save(any(ToDoList.class));

        assertNotNull(result);
        assertEquals(createToDoListDto.getType(), result.getType());
        assertEquals(createToDoListDto.getTitle(), result.getTitle());
        assertEquals(createToDoListDto.getDescription(), result.getDescription());
        assertEquals(createToDoListDto.getMemo(), result.getMemo());
        assertEquals(createToDoListDto.isDone(), result.isDone());
    }

    @Test
    @DisplayName("ToDoList 생성 실패 - JD를 찾을 수 없음")
    void createToDoList_jdNotFound() {
        // Given
        when(jdRepository.findById(jdId)).thenReturn(Optional.empty());

        // When & Then
        JDException exception = assertThrows(JDException.class, () ->
                toDoListService.createToDoList(jdId, createToDoListDto)
        );

        assertEquals(JDErrorCode.JD_NOT_FOUND, exception.getErrorCode());
        assertEquals("JD를 찾을 수 없습니다.", exception.getMessage());

        verify(toDoListRepository, never()).save(any(ToDoList.class));
    }

    @Test
    @DisplayName("ToDoList 성공적으로 수정")
    void updateToDoList_success() {
        // Given
        when(jdRepository.findById(jdId)).thenReturn(Optional.of(mockJd));
        when(toDoListRepository.findById(toDoListId)).thenReturn(Optional.of(mockToDoList));
        when(toDoListRepository.save(any(ToDoList.class))).thenAnswer(invocation -> invocation.<ToDoList>getArgument(0));


        // When
        ToDoListResponseDto result = toDoListService.updateToDoList(jdId, toDoListId, updateToDoListDto);

        // Then
        verify(jdRepository, times(1)).findById(jdId);
        verify(toDoListRepository, times(1)).findById(toDoListId);
        verify(toDoListRepository, times(1)).save(any(ToDoList.class));

        assertNotNull(result);
        assertEquals(toDoListId, result.getChecklist_id());
        assertEquals(updateToDoListDto.getType(), result.getType());
        assertEquals(updateToDoListDto.getTitle(), result.getTitle());
        assertEquals(updateToDoListDto.getDescription(), result.getDescription());
        assertEquals(updateToDoListDto.getMemo(), result.getMemo());
        assertEquals(updateToDoListDto.isDone(), result.isDone());
        assertEquals(jdId, result.getJdId());
    }

    @Test
    @DisplayName("ToDoList 수정 실패 - JD를 찾을 수 없음")
    void updateToDoList_failure_jdNotFound() {
        // Given
        when(jdRepository.findById(jdId)).thenReturn(Optional.empty());

        // When & Then
        JDException exception = assertThrows(JDException.class, () ->
                toDoListService.updateToDoList(jdId, toDoListId, updateToDoListDto)
        );

        assertEquals(JDErrorCode.JD_NOT_FOUND, exception.getErrorCode());
        assertEquals("JD를 찾을 수 없습니다.", exception.getMessage());

        verify(toDoListRepository, never()).findById(anyLong());
        verify(toDoListRepository, never()).save(any(ToDoList.class));
    }

    @Test
    @DisplayName("ToDoList 수정 실패 - ToDoList를 찾을 수 없음")
    void updateToDoList_failure_toDoListNotFound() {
        // Given
        when(jdRepository.findById(jdId)).thenReturn(Optional.of(mockJd));
        when(toDoListRepository.findById(toDoListId)).thenReturn(Optional.empty());

        // When & Then
        ToDoListException exception = assertThrows(ToDoListException.class, () ->
                toDoListService.updateToDoList(jdId, toDoListId, updateToDoListDto)
        );

        assertEquals(ToDoListErrorCode.TODO_LIST_NOT_FOUND, exception.getErrorCode());
        assertEquals("ToDoList를 찾을 수 없습니다.", exception.getMessage());
        verify(toDoListRepository, never()).save(any(ToDoList.class));
    }

    @Test
    @DisplayName("ToDoList 수정 실패 - 해당 JD에 속하지 않음")
    void updateToDoList_notBelongToJd() {
        // Given
        Long anotherJdId = 99L;
        JD anotherMockJd = JD.builder()
                .id(anotherJdId)
                .title("다른 JD")
                .jdUrl("https://www.test.com")
                .endedAt(LocalDate.now())
                .build();

        when(jdRepository.findById(jdId)).thenReturn(Optional.of(mockJd));
        ToDoList toDoListBelongingToAnotherJd = ToDoList.builder()
                .category(ToDoListType.EMPLOYMENT_SCHEDULE_RELATED)
                .title("다른 JD의 ToDo")
                .content("설명")
                .memo("메모")
                .isDone(false)
                .jd(anotherMockJd)
                .build();
        when(toDoListRepository.findById(toDoListId)).thenReturn(Optional.of(toDoListBelongingToAnotherJd));

        // When & Then
        ToDoListException exception = assertThrows(ToDoListException.class, () ->
                toDoListService.updateToDoList(jdId, toDoListId, updateToDoListDto)
        );

        assertEquals(ToDoListErrorCode.TODO_LIST_NOT_BELONG_TO_JD, exception.getErrorCode());
        assertEquals("해당 JD에 속하지 않는 ToDoList입니다.", exception.getMessage());
        verify(toDoListRepository, never()).save(any(ToDoList.class));
    }

    @Test
    @DisplayName("ToDoList 성공적으로 조회")
    void getToDoList_success() {
        // Given
        when(jdRepository.findById(jdId)).thenReturn(Optional.of(mockJd));
        when(toDoListRepository.findById(toDoListId)).thenReturn(Optional.of(mockToDoList));

        // When
        ToDoListResponseDto result = toDoListService.getToDoList(jdId, toDoListId);

        // Then
        verify(jdRepository, times(1)).findById(jdId);
        verify(toDoListRepository, times(1)).findById(toDoListId);

        assertNotNull(result);
        assertEquals(toDoListId, result.getChecklist_id());
        assertEquals(mockToDoList.getCategory(), result.getType());
        assertEquals(mockToDoList.getTitle(), result.getTitle());
        assertEquals(mockToDoList.getContent(), result.getDescription());
        assertEquals(mockToDoList.getMemo(), result.getMemo());
        assertEquals(mockToDoList.isDone(), result.isDone());
        assertEquals(jdId, result.getJdId());
    }

    @Test
    @DisplayName("ToDoList 조회 실패 - JD를 찾을 수 없음")
    void getToDoList_failure_jdNotFound() {
        // Given
        when(jdRepository.findById(jdId)).thenReturn(Optional.empty());

        // When & Then
        JDException exception = assertThrows(JDException.class, () ->
                toDoListService.getToDoList(jdId, toDoListId)
        );

        assertEquals(JDErrorCode.JD_NOT_FOUND, exception.getErrorCode());
        assertEquals("JD를 찾을 수 없습니다.", exception.getMessage());

        verify(toDoListRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("ToDoList 조회 실패 - ToDoList를 찾을 수 없음")
    void getToDoList_toDoListNotFound() {
        // Given
        when(jdRepository.findById(jdId)).thenReturn(Optional.of(mockJd));
        when(toDoListRepository.findById(toDoListId)).thenReturn(Optional.empty());

        // When & Then
        ToDoListException exception = assertThrows(ToDoListException.class, () ->
                toDoListService.getToDoList(jdId, toDoListId)
        );

        assertEquals(ToDoListErrorCode.TODO_LIST_NOT_FOUND, exception.getErrorCode());
        assertEquals("ToDoList를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("ToDoList 조회 실패 - 해당 JD에 속하지 않음")
    void getToDoList_notBelongToJd() {
        // Given
        Long anotherJdId = 99L;
        JD anotherMockJd = JD.builder()
                .id(anotherJdId)
                .title("다른 JD")
                .jdUrl("https://www.test.com")
                .endedAt(LocalDate.now())
                .build();

        ToDoList toDoListBelongingToAnotherJd = ToDoList.builder()
                .id(toDoListId)
                .category(ToDoListType.EMPLOYMENT_SCHEDULE_RELATED)
                .title("다른 JD의 ToDo")
                .content("설명")
                .memo("메모")
                .isDone(false)
                .jd(anotherMockJd)
                .build();

        when(jdRepository.findById(jdId)).thenReturn(Optional.of(mockJd));
        when(toDoListRepository.findById(toDoListId)).thenReturn(Optional.of(toDoListBelongingToAnotherJd));

        // When & Then
        ToDoListException exception = assertThrows(ToDoListException.class, () ->
                toDoListService.getToDoList(jdId, toDoListId)
        );

        assertEquals(ToDoListErrorCode.TODO_LIST_NOT_BELONG_TO_JD, exception.getErrorCode());
        assertEquals("해당 JD에 속하지 않는 ToDoList입니다.", exception.getMessage());
    }
}