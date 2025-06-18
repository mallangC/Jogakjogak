package com.zb.jogakjogak.jobDescription.service;

import com.github.javafaker.Faker;
import com.zb.jogakjogak.global.exception.JDErrorCode;
import com.zb.jogakjogak.global.exception.JDException;
import com.zb.jogakjogak.global.exception.ToDoListErrorCode;
import com.zb.jogakjogak.global.exception.ToDoListException;
import com.zb.jogakjogak.jobDescription.domain.requestDto.BulkToDoListUpdateRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.ToDoListDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.ToDoListUpdateRequestDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListDeleteResponseDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListGetByCategoryResponseDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListResponseDto;
import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.jobDescription.entity.ToDoList;
import com.zb.jogakjogak.jobDescription.repository.JDRepository;
import com.zb.jogakjogak.jobDescription.repository.ToDoListRepository;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import com.zb.jogakjogak.security.Role;
import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ToDoListServiceTest {

    @InjectMocks
    private ToDoListService toDoListService;

    @Mock
    private ToDoListRepository toDoListRepository;

    @Mock
    private JDRepository jdRepository;

    @Mock
    private MemberRepository memberRepository;

    private Long jdId;
    @Mock
    private JD mockJd;
    private Long toDoListId;
    private ToDoList mockToDoList;
    private ToDoListDto createToDoListDto;
    private ToDoListDto updateToDoListDto;
    private ToDoListUpdateRequestDto createToDoListUpdateReqDto;
    private ToDoListUpdateRequestDto updateToDoListUpdateReqDto;
    private ToDoListType targetCategory;
    private Faker faker;
    private Member mockMember;

    @BeforeEach
    void setUp() {
        jdId = 1L;
        toDoListId = 101L;
        faker = new Faker();
        targetCategory = ToDoListType.STRUCTURAL_COMPLEMENT_PLAN;

        mockMember = Member.builder()
                .id(1L)
                .role(Role.USER)
                .userName(faker.name().fullName())
                .build();

        mockJd = JD.builder()
                .id(jdId)
                .title(faker.job().title())
                .jdUrl(faker.internet().url())
                .companyName(faker.company().name())
                .job(faker.job().position())
                .content(faker.lorem().paragraph())
                .endedAt(faker.date().future(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .applyAt(null)
                .member(mockMember)
                .memo(faker.lorem().sentence())
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

        createToDoListUpdateReqDto = ToDoListUpdateRequestDto.builder()
                .id(null)
                .title(faker.lorem().sentence(3, 5))
                .category(targetCategory)
                .content(faker.lorem().paragraph(2))
                .memo(faker.lorem().sentence(1))
                .isDone(faker.bool().bool())
                .jdId(jdId)
                .build();

        updateToDoListUpdateReqDto = ToDoListUpdateRequestDto.builder()
                .id(toDoListId)
                .title(faker.lorem().sentence(4, 6))
                .category(targetCategory)
                .content(faker.lorem().paragraph(3))
                .memo(faker.lorem().sentence(2))
                .isDone(!createToDoListUpdateReqDto.isDone())
                .jdId(jdId)
                .build();

        mockToDoList = ToDoList.builder()
                .id(toDoListId)
                .category(createToDoListDto.getCategory())
                .title(createToDoListDto.getTitle())
                .content(createToDoListDto.getContent())
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
                    .id(1L)
                    .category(originalToDoList.getCategory())
                    .title(originalToDoList.getTitle())
                    .content(originalToDoList.getContent())
                    .memo(originalToDoList.getMemo())
                    .isDone(originalToDoList.isDone())
                    .jd(originalToDoList.getJd())
                    .build();
        });
        when(memberRepository.findByUserName(mockMember.getName())).thenReturn(Optional.of(mockMember));

        // When
        ToDoListResponseDto result = toDoListService.createToDoList(jdId, createToDoListDto, mockMember.getName());

        // Then
        verify(jdRepository, times(1)).findById(jdId);
        verify(toDoListRepository, times(1)).save(any(ToDoList.class));

        assertNotNull(result);
        assertEquals(createToDoListDto.getCategory(), result.getCategory());
        assertEquals(createToDoListDto.getTitle(), result.getTitle());
        assertEquals(createToDoListDto.getContent(), result.getContent());
        assertEquals(createToDoListDto.getMemo(), result.getMemo());
        assertEquals(createToDoListDto.isDone(), result.isDone());
        assertEquals(jdId, result.getJdId());
    }

    @Test
    @DisplayName("ToDoList 생성 실패 - JD를 찾을 수 없음")
    void createToDoList_jdNotFound() {
        // Given
        when(memberRepository.findByUserName(mockMember.getName())).thenReturn(Optional.of(mockMember));
        when(jdRepository.findById(jdId)).thenReturn(Optional.empty());

        // When & Then
        JDException exception = assertThrows(JDException.class, () ->
                toDoListService.createToDoList(jdId, createToDoListDto, mockMember.getName())
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
        when(memberRepository.findByUserName(mockMember.getName())).thenReturn(Optional.of(mockMember));
        when(toDoListRepository.save(any(ToDoList.class))).thenAnswer(invocation -> invocation.<ToDoList>getArgument(0));


        // When
        ToDoListResponseDto result = toDoListService.updateToDoList(jdId, toDoListId, updateToDoListDto, mockMember.getName());

        // Then
        verify(jdRepository, times(2)).findById(jdId);
        verify(memberRepository, times(1)).findByUserName(mockMember.getName());
        verify(toDoListRepository, times(1)).findById(toDoListId);
        verify(toDoListRepository, times(1)).save(any(ToDoList.class));

        assertNotNull(result);
        assertEquals(toDoListId, result.getChecklist_id());
        assertEquals(updateToDoListDto.getCategory(), result.getCategory());
        assertEquals(updateToDoListDto.getTitle(), result.getTitle());
        assertEquals(updateToDoListDto.getContent(), result.getContent());
        assertEquals(updateToDoListDto.getMemo(), result.getMemo());
        assertEquals(updateToDoListDto.isDone(), result.isDone());
        assertEquals(jdId, result.getJdId());
    }


    @Test
    @DisplayName("ToDoList 성공적으로 조회")
    void getToDoList_success() {
        // Given
        when(memberRepository.findByUserName(mockMember.getName())).thenReturn(Optional.of(mockMember));
        when(jdRepository.findById(jdId)).thenReturn(Optional.of(mockJd));
        when(toDoListRepository.findById(toDoListId)).thenReturn(Optional.of(mockToDoList));

        // When
        ToDoListResponseDto result = toDoListService.getToDoList(jdId, toDoListId, mockMember.getName());

        // Then
        verify(jdRepository, times(2)).findById(jdId);
        verify(memberRepository, times(1)).findByUserName(mockMember.getName());

        assertNotNull(result);
        assertEquals(toDoListId, result.getChecklist_id());
        assertEquals(mockToDoList.getCategory(), result.getCategory());
        assertEquals(mockToDoList.getTitle(), result.getTitle());
        assertEquals(mockToDoList.getContent(), result.getContent());
        assertEquals(mockToDoList.getMemo(), result.getMemo());
        assertEquals(mockToDoList.isDone(), result.isDone());
        assertEquals(jdId, result.getJdId());
    }

    @Test
    @DisplayName("ToDoList 성공적으로 삭제")
    void deleteToDoList_success() {
        // Given
        ToDoList mockToDoList = mock(ToDoList.class);
        when(mockToDoList.getJd()).thenReturn(mockJd);

        when(jdRepository.findById(jdId)).thenReturn(Optional.of(mockJd));
        when(memberRepository.findByUserName(mockMember.getName())).thenReturn(Optional.of(mockMember));
        when(toDoListRepository.findById(toDoListId)).thenReturn(Optional.of(mockToDoList));

        // When
        toDoListService.deleteToDoList(jdId, toDoListId, mockMember.getName());

        // Then
        verify(toDoListRepository).delete(mockToDoList);
    }

    @Test
    @DisplayName("JD를 찾을 수 없을 때 모든 ToDoList 관련 작업 실패")
    void allToDoListOperations_fail_jdNotFound() {

        // When & Then
        BulkToDoListUpdateRequestDto bulkReq = BulkToDoListUpdateRequestDto.builder()
                .category(targetCategory)
                .updatedOrCreateToDoLists(Collections.emptyList())
                .deletedToDoListIds(Collections.emptyList())
                .build();
        verify(toDoListRepository, never()).save(any(ToDoList.class));
        verify(toDoListRepository, never()).deleteAllById(anyList());

        verify(toDoListRepository, never()).findByIdAndJdId(anyLong(), anyLong());
        verify(toDoListRepository, never()).save(any(ToDoList.class));
        verify(toDoListRepository, never()).findByIdAndJdId(anyLong(), anyLong());
        verify(toDoListRepository, never()).findByIdAndJdId(anyLong(), anyLong());
        verify(toDoListRepository, never()).delete(any(ToDoList.class));
        verify(toDoListRepository, never()).findByJdAndCategory(any(JD.class), any(ToDoListType.class));
    }


    @Test
    @DisplayName("ToDoList를 찾을 수 없을 때 업데이트, 조회, 삭제 작업 실패")
    void updateGetDelete_fail_toDoListNotFound() {
        // Given
        when(jdRepository.findById(jdId)).thenReturn(Optional.of(mockJd));
        when(memberRepository.findByUserName(mockMember.getName())).thenReturn(Optional.of(mockMember));
        when(toDoListRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrowsToDoListNotFound(() ->
                toDoListService.updateToDoList(jdId, toDoListId, updateToDoListDto, mockMember.getName()));
        verify(toDoListRepository, never()).save(any(ToDoList.class));

        assertThrowsToDoListNotFound(() ->
                toDoListService.getToDoList(jdId, toDoListId, mockMember.getName()));

        assertThrowsToDoListNotFound(() ->
                toDoListService.deleteToDoList(jdId, toDoListId, mockMember.getName()));
        verify(toDoListRepository, never()).delete(any(ToDoList.class));

        BulkToDoListUpdateRequestDto bulkReq = BulkToDoListUpdateRequestDto.builder()
                .category(targetCategory)
                .updatedOrCreateToDoLists(Collections.singletonList(updateToDoListUpdateReqDto))
                .deletedToDoListIds(Collections.emptyList())
                .build();
        assertThrowsToDoListNotFound(() ->
                toDoListService.bulkUpdateToDoLists(jdId, bulkReq));
    }

    @Test
    @DisplayName("ToDoList가 해당 JD에 속하지 않을 때 업데이트, 조회, 삭제 작업 실패")
    void updateGetDelete_fail_notBelongToJd() {
        // Given
        Long anotherJdId = 99L;
        JD anotherMockJd = JD.builder()
                .id(anotherJdId)
                .title("다른 JD")
                .jdUrl("https://www.test.com")
                .companyName(faker.company().name())
                .job(faker.job().position())
                .content(faker.lorem().paragraph())
                .endedAt(LocalDate.now())
                .memo(faker.lorem().sentence())
                .build();

        ToDoList toDoListForBulkBelongingToAnotherJd = mock(ToDoList.class);
        when(toDoListForBulkBelongingToAnotherJd.getJd()).thenReturn(anotherMockJd);
        when(memberRepository.findByUserName(mockMember.getName())).thenReturn(Optional.of(mockMember));
        when(jdRepository.findById(jdId)).thenReturn(Optional.of(mockJd));


        assertThrowsToDoListNotFound(() ->
                toDoListService.updateToDoList(jdId, toDoListId, updateToDoListDto, mockMember.getName()));
        verify(toDoListRepository, never()).save(any(ToDoList.class));

        assertThrowsToDoListNotFound(() ->
                toDoListService.getToDoList(jdId, toDoListId, mockMember.getName()));

        assertThrowsToDoListNotFound(() ->
                toDoListService.deleteToDoList(jdId, toDoListId, mockMember.getName()));
        verify(toDoListRepository, never()).delete(any(ToDoList.class));

        BulkToDoListUpdateRequestDto bulkReqWrongJd = BulkToDoListUpdateRequestDto.builder()
                .category(targetCategory)
                .updatedOrCreateToDoLists(Collections.singletonList(updateToDoListUpdateReqDto))
                .deletedToDoListIds(Collections.emptyList())
                .build();
        assertThrowsToDoListNotFound(() ->
                toDoListService.bulkUpdateToDoLists(jdId, bulkReqWrongJd));

        List<Long> deletedIdsWrongJd = Collections.singletonList(toDoListId);
        BulkToDoListUpdateRequestDto bulkReqDeleteWrongJd = BulkToDoListUpdateRequestDto.builder()
                .category(targetCategory)
                .updatedOrCreateToDoLists(Collections.emptyList())
                .deletedToDoListIds(deletedIdsWrongJd)
                .build();
        when(toDoListRepository.findAllById(deletedIdsWrongJd)).thenReturn(Collections.singletonList(toDoListForBulkBelongingToAnotherJd));
        assertThrowsToDoListNotBelongToJd(() ->
                toDoListService.bulkUpdateToDoLists(jdId, bulkReqDeleteWrongJd));

        ToDoListUpdateRequestDto wrongJdInDto = ToDoListUpdateRequestDto.builder()
                .id(toDoListId)
                .title("Wrong JD DTO")
                .category(targetCategory)
                .content("Content")
                .memo("Memo")
                .isDone(false)
                .jdId(anotherJdId)
                .build();
        BulkToDoListUpdateRequestDto bulkReqInitialWrongJd = BulkToDoListUpdateRequestDto.builder()
                .category(targetCategory)
                .updatedOrCreateToDoLists(Collections.singletonList(wrongJdInDto))
                .deletedToDoListIds(Collections.emptyList())
                .build();
        assertThrowsToDoListNotBelongToJd(() ->
                toDoListService.bulkUpdateToDoLists(jdId, bulkReqInitialWrongJd));
    }

    @Test
    @DisplayName("Bulk Update: 성공적으로 여러 ToDoList 생성, 수정, 삭제")
    void bulkUpdateToDoLists_success() {
        // Given
        ToDoList existingToDoListForUpdate = mock(ToDoList.class);

        ToDoListUpdateRequestDto newToDoListDto = ToDoListUpdateRequestDto.builder()
                .id(null)
                .title(faker.lorem().sentence())
                .category(targetCategory)
                .content(faker.lorem().paragraph())
                .memo(faker.lorem().sentence())
                .isDone(faker.bool().bool())
                .jdId(jdId)
                .build();

        Long anotherExistingId = 102L;
        ToDoList anotherExistingToDoList = mock(ToDoList.class);
        when(anotherExistingToDoList.getJd()).thenReturn(mockJd);
        when(anotherExistingToDoList.getCategory()).thenReturn(targetCategory);

        ToDoListUpdateRequestDto updateAnotherDto = ToDoListUpdateRequestDto.builder()
                .id(anotherExistingId)
                .title("Updated Another Title")
                .category(targetCategory)
                .content("Updated Another Content")
                .memo("Updated Another Memo")
                .isDone(true)
                .jdId(jdId)
                .build();


        Long deletedToDoListId = 103L;
        ToDoList toDoListToDelete = mock(ToDoList.class); // <-- Mock this entity
        when(toDoListToDelete.getId()).thenReturn(deletedToDoListId);
        when(toDoListToDelete.getJd()).thenReturn(mockJd);
        when(toDoListToDelete.getCategory()).thenReturn(targetCategory);


        BulkToDoListUpdateRequestDto request = BulkToDoListUpdateRequestDto.builder()
                .category(targetCategory)
                .updatedOrCreateToDoLists(Arrays.asList(newToDoListDto, updateAnotherDto))
                .deletedToDoListIds(Collections.singletonList(deletedToDoListId))
                .build();


        when(jdRepository.findById(jdId)).thenReturn(Optional.of(mockJd));
        when(toDoListRepository.findById(anotherExistingId)).thenReturn(Optional.of(anotherExistingToDoList));
        when(toDoListRepository.save(any(ToDoList.class))).thenAnswer(invocation -> {
            ToDoList savedToDo = invocation.getArgument(0);
            if (savedToDo.getId() == null) {
                savedToDo = ToDoList.builder()
                        .id(faker.number().randomNumber() + 1000L)
                        .category(savedToDo.getCategory())
                        .title(savedToDo.getTitle())
                        .content(savedToDo.getContent())
                        .memo(savedToDo.getMemo())
                        .isDone(savedToDo.isDone())
                        .jd(savedToDo.getJd())
                        .build();
            }
            return savedToDo;
        });

        when(toDoListRepository.findAllById(Collections.singletonList(deletedToDoListId)))
                .thenReturn(Collections.singletonList(toDoListToDelete));
        doNothing().when(toDoListRepository).deleteAllById(Collections.singletonList(deletedToDoListId));

        ToDoList finalResultNew = mock(ToDoList.class);
        when(finalResultNew.getId()).thenReturn(newToDoListDto.getId() != null ? newToDoListDto.getId() : faker.number().randomNumber() + 1000L);
        when(finalResultNew.getCategory()).thenReturn(newToDoListDto.getCategory());
        when(finalResultNew.getTitle()).thenReturn(newToDoListDto.getTitle());
        when(finalResultNew.getContent()).thenReturn(newToDoListDto.getContent());
        when(finalResultNew.getMemo()).thenReturn(newToDoListDto.getMemo());
        when(finalResultNew.isDone()).thenReturn(newToDoListDto.isDone());
        when(finalResultNew.getJd()).thenReturn(mockJd);

        ToDoList finalResultUpdatedAnother = mock(ToDoList.class);
        when(finalResultUpdatedAnother.getId()).thenReturn(updateAnotherDto.getId());
        when(finalResultUpdatedAnother.getCategory()).thenReturn(updateAnotherDto.getCategory());
        when(finalResultUpdatedAnother.getTitle()).thenReturn(updateAnotherDto.getTitle());
        when(finalResultUpdatedAnother.getContent()).thenReturn(updateAnotherDto.getContent());
        when(finalResultUpdatedAnother.getMemo()).thenReturn(updateAnotherDto.getMemo());
        when(finalResultUpdatedAnother.isDone()).thenReturn(updateAnotherDto.isDone());
        when(finalResultUpdatedAnother.getJd()).thenReturn(mockJd);

        when(toDoListRepository.findByJdAndCategory(mockJd, targetCategory)).thenReturn(Arrays.asList(finalResultNew, finalResultUpdatedAnother));


        // When
        ToDoListGetByCategoryResponseDto result = toDoListService.bulkUpdateToDoLists(jdId, request);

        // Then
        verify(jdRepository, times(1)).findById(jdId);

        verify(toDoListRepository, times(1)).findById(anotherExistingId);
        verify(anotherExistingToDoList, times(1)).updateFromBulkUpdateToDoLists(any(ToDoListUpdateRequestDto.class));
        verify(toDoListRepository, times(1)).save(any(ToDoList.class));
        verify(toDoListRepository, times(1)).findAllById(Collections.singletonList(deletedToDoListId));
        verify(toDoListRepository, times(1)).deleteAllById(Collections.singletonList(deletedToDoListId));
        verify(toDoListRepository, times(1)).findByJdAndCategory(mockJd, targetCategory);

        assertNotNull(result);
        assertEquals(jdId, result.getJdId());
        assertEquals(targetCategory, result.getCategory());
        assertNotNull(result.getToDoLists());
        assertEquals(2, result.getToDoLists().size());

        assertTrue(result.getToDoLists().stream().anyMatch(dto -> dto.getTitle().equals(newToDoListDto.getTitle())));
        assertTrue(result.getToDoLists().stream().anyMatch(dto -> dto.getTitle().equals(updateAnotherDto.getTitle())));
    }

    @Test
    @DisplayName("Bulk Update: 카테고리가 누락되면 실패")
    void bulkUpdateToDoLists_fail_categoryRequired() {
        // Given
        BulkToDoListUpdateRequestDto request = BulkToDoListUpdateRequestDto.builder()
                .category(null)
                .updatedOrCreateToDoLists(Collections.emptyList())
                .deletedToDoListIds(Collections.emptyList())
                .build();

        // When & Then
        assertThrowsJdNotFound(() -> toDoListService.bulkUpdateToDoLists(jdId, request));

        verify(toDoListRepository, never()).save(any(ToDoList.class));
        verify(toDoListRepository, never()).deleteAllById(anyList());
    }

    @Test
    @DisplayName("Bulk Update: 업데이트/삭제 시 ToDoList가 해당 JD/카테고리에 속하지 않으면 실패")
    void bulkUpdateToDoLists_fail_notBelongToJdOrCategory() {
        // Given
        Long anotherJdId = 99L;
        JD anotherMockJd = JD.builder()
                .id(anotherJdId)
                .title("Another JD")
                .jdUrl("https://www.test.com")
                .companyName(faker.company().name())
                .job(faker.job().position())
                .content(faker.lorem().paragraph())
                .endedAt(LocalDate.now())
                .memo(faker.lorem().sentence())
                .build();

        ToDoListUpdateRequestDto dtoWithWrongJdId = ToDoListUpdateRequestDto.builder()
                .id(null)
                .title("Wrong JD DTO")
                .category(targetCategory)
                .content("Content")
                .memo("Memo")
                .isDone(false)
                .jdId(anotherJdId)
                .build();
        BulkToDoListUpdateRequestDto requestWrongJdInDto = BulkToDoListUpdateRequestDto.builder()
                .category(targetCategory)
                .updatedOrCreateToDoLists(Collections.singletonList(dtoWithWrongJdId))
                .deletedToDoListIds(Collections.emptyList())
                .build();

        ToDoListUpdateRequestDto dtoWithWrongCategory = ToDoListUpdateRequestDto.builder()
                .id(null)
                .title("Wrong Category DTO")
                .category(ToDoListType.CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL) // Incorrect category
                .content("Content")
                .memo("Memo")
                .isDone(false)
                .jdId(jdId)
                .build();
        BulkToDoListUpdateRequestDto requestWrongCategoryInDto = BulkToDoListUpdateRequestDto.builder()
                .category(targetCategory)
                .updatedOrCreateToDoLists(Collections.singletonList(dtoWithWrongCategory))
                .deletedToDoListIds(Collections.emptyList())
                .build();

        Long deletedIdWrongOwner = 200L;
        ToDoList toDoListWrongOwner = mock(ToDoList.class);
        when(toDoListWrongOwner.getJd()).thenReturn(anotherMockJd);

        BulkToDoListUpdateRequestDto requestDeleteWrongOwner = BulkToDoListUpdateRequestDto.builder()
                .category(targetCategory)
                .updatedOrCreateToDoLists(Collections.emptyList())
                .deletedToDoListIds(Collections.singletonList(deletedIdWrongOwner))
                .build();


        when(jdRepository.findById(jdId)).thenReturn(Optional.of(mockJd));
        when(toDoListRepository.findAllById(Collections.singletonList(deletedIdWrongOwner)))
                .thenReturn(Collections.singletonList(toDoListWrongOwner));


        // When & Then for Scenario 1
        assertThrowsToDoListNotBelongToJd(() -> toDoListService.bulkUpdateToDoLists(jdId, requestWrongJdInDto));

        // When & Then for Scenario 2
        assertThrowsToDoListNotBelongToJd(() -> toDoListService.bulkUpdateToDoLists(jdId, requestWrongCategoryInDto));

        // When & Then for Scenario 3
        assertThrowsToDoListNotBelongToJd(() -> toDoListService.bulkUpdateToDoLists(jdId, requestDeleteWrongOwner));

        // Verify no changes were persisted
        verify(toDoListRepository, never()).save(any(ToDoList.class));
        verify(toDoListRepository, never()).deleteAllById(anyList());
    }

    // --- New Test Methods for getToDoListsByJdAndCategory ---

    @Test
    @DisplayName("Get ToDoLists By Category: 성공적으로 조회")
    void getToDoListsByJdAndCategory_success() {
        // Given
        // Prepare some mock ToDoLists that belong to the target category and JD
        ToDoList toDoList1 = mock(ToDoList.class); // Mock the entity
        when(toDoList1.getId()).thenReturn(1L);
        when(toDoList1.getCategory()).thenReturn(targetCategory);
        when(toDoList1.getTitle()).thenReturn("ToDo 1");
        when(toDoList1.getContent()).thenReturn("Content 1");
        when(toDoList1.getMemo()).thenReturn("Memo 1");
        when(toDoList1.isDone()).thenReturn(false);
        when(toDoList1.getJd()).thenReturn(mockJd);

        ToDoList toDoList2 = mock(ToDoList.class); // Mock the entity
        when(toDoList2.getId()).thenReturn(2L);
        when(toDoList2.getCategory()).thenReturn(targetCategory);
        when(toDoList2.getTitle()).thenReturn("ToDo 2");
        when(toDoList2.getContent()).thenReturn("Content 2");
        when(toDoList2.getMemo()).thenReturn("Memo 2");
        when(toDoList2.isDone()).thenReturn(true);
        when(toDoList2.getJd()).thenReturn(mockJd);

        List<ToDoList> mockToDoLists = Arrays.asList(toDoList1, toDoList2);

        when(jdRepository.findById(jdId)).thenReturn(Optional.of(mockJd));
        when(toDoListRepository.findByJdAndCategory(mockJd, targetCategory)).thenReturn(mockToDoLists);

        // When
        ToDoListGetByCategoryResponseDto result = toDoListService.getToDoListsByJdAndCategory(jdId, targetCategory);

        // Then
        verify(jdRepository, times(1)).findById(jdId);
        verify(toDoListRepository, times(1)).findByJdAndCategory(mockJd, targetCategory);

        assertNotNull(result);
        assertEquals(jdId, result.getJdId());
        assertEquals(targetCategory, result.getCategory());
        assertNotNull(result.getToDoLists());
        assertEquals(2, result.getToDoLists().size());

        // Verify content of returned DTOs
        assertTrue(result.getToDoLists().stream().anyMatch(dto -> dto.getTitle().equals("ToDo 1")));
        assertTrue(result.getToDoLists().stream().anyMatch(dto -> dto.getTitle().equals("ToDo 2")));
    }

    @Test
    @DisplayName("Get ToDoLists By Category: JD를 찾을 수 없을 때 실패")
    void getToDoListsByJdAndCategory_fail_jdNotFound() {
        // Given
        when(jdRepository.findById(jdId)).thenReturn(Optional.empty());

        // When & Then
        assertThrowsJdNotFound(() -> toDoListService.getToDoListsByJdAndCategory(jdId, targetCategory));

        verify(toDoListRepository, never()).findByJdAndCategory(any(JD.class), any(ToDoListType.class));
    }

    @Test
    @DisplayName("Get ToDoLists By Category: 해당 카테고리에 ToDoList가 없을 때 빈 리스트 반환")
    void getToDoListsByJdAndCategory_success_emptyList() {
        // Given
        when(jdRepository.findById(jdId)).thenReturn(Optional.of(mockJd));
        when(toDoListRepository.findByJdAndCategory(mockJd, targetCategory)).thenReturn(Collections.emptyList());

        // When
        ToDoListGetByCategoryResponseDto result = toDoListService.getToDoListsByJdAndCategory(jdId, targetCategory);

        // Then
        verify(jdRepository, times(1)).findById(jdId);
        verify(toDoListRepository, times(1)).findByJdAndCategory(mockJd, targetCategory);

        assertNotNull(result);
        assertEquals(jdId, result.getJdId());
        assertEquals(targetCategory, result.getCategory());
        assertNotNull(result.getToDoLists());
        assertTrue(result.getToDoLists().isEmpty());
    }

    private void assertThrowsJdNotFound(Executable executable) {
        JDException exception = assertThrows(JDException.class, executable);
        assertEquals(JDErrorCode.JD_NOT_FOUND, exception.getErrorCode());
        assertEquals("JD를 찾을 수 없습니다.", exception.getMessage());
    }

    private void assertThrowsToDoListNotFound(Executable executable) {
        ToDoListException exception = assertThrows(ToDoListException.class, executable);
        assertEquals(ToDoListErrorCode.TODO_LIST_NOT_FOUND, exception.getErrorCode());
        assertEquals("ToDoList를 찾을 수 없습니다.", exception.getMessage());
    }

    private void assertThrowsToDoListNotBelongToJd(Executable executable) {
        ToDoListException exception = assertThrows(ToDoListException.class, executable);
        assertEquals(ToDoListErrorCode.TODO_LIST_NOT_BELONG_TO_JD, exception.getErrorCode());
        assertEquals("해당 JD에 속하지 않는 ToDoList입니다.", exception.getMessage());
    }

}
