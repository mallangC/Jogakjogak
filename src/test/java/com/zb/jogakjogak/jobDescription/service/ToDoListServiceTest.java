package com.zb.jogakjogak.jobDescription.service;

import com.github.javafaker.Faker;
import com.zb.jogakjogak.global.exception.JDErrorCode;
import com.zb.jogakjogak.global.exception.JDException;
import com.zb.jogakjogak.global.exception.ToDoListErrorCode;
import com.zb.jogakjogak.global.exception.ToDoListException;
import com.zb.jogakjogak.jobDescription.domain.requestDto.*;
import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListGetByCategoryResponseDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListResponseDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.UpdateIsDoneTodoListsResponseDto;
import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.jobDescription.entity.ToDoList;
import com.zb.jogakjogak.jobDescription.repository.JDRepository;
import com.zb.jogakjogak.jobDescription.repository.ToDoListRepository;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import com.zb.jogakjogak.security.Role;
import com.zb.jogakjogak.security.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Captor
    private ArgumentCaptor<List<ToDoList>> toDoListListCaptor;

    @Captor
    private ArgumentCaptor<List<Long>> idListCaptor;

    private Long jdId;
    private Long toDoListId;
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
                .username("testUser")
                .build();

    }

    @Test
    @DisplayName("ToDoList 성공적으로 생성")
    void createToDoList_success() {
        // Given
        JD mockJd = createTestJd(jdId, mockMember, new ArrayList<>());
        CreateToDoListRequestDto createToDoListDto = CreateToDoListRequestDto.builder()
                .title("ToDoList 제목")
                .category(targetCategory)
                .content("ToDoList 내용")
                .build();

        when(jdRepository.findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId())).thenReturn(Optional.of(mockJd));
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

        // When
        ToDoListResponseDto result = toDoListService.createToDoList(jdId, createToDoListDto, mockMember);

        // Then
        verify(jdRepository, times(1)).findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId());
        verify(toDoListRepository, times(1)).save(any(ToDoList.class));

        assertNotNull(result);
        assertEquals(createToDoListDto.getCategory(), result.getCategory());
        assertEquals(createToDoListDto.getTitle(), result.getTitle());
        assertEquals(createToDoListDto.getContent(), result.getContent());
        assertEquals("", result.getMemo());
        assertEquals(false, result.isDone());
        assertEquals(jdId, result.getJdId());
    }

    @Test
    @DisplayName("ToDoList 생성 실패 - JD를 찾을 수 없음")
    void createToDoList_jdNotFound() {
        // Given
        CreateToDoListRequestDto createToDoListDto = new CreateToDoListRequestDto();
        when(jdRepository.findJdWithMemberAndToDoListsByIdAndMemberId(anyLong(), eq(mockMember.getId()))).thenReturn(Optional.empty());

        // When & Then
        JDException exception = assertThrows(JDException.class, () ->
                toDoListService.createToDoList(jdId, createToDoListDto, mockMember)
        );

        assertEquals(JDErrorCode.UNAUTHORIZED_ACCESS, exception.getErrorCode());
        assertEquals("해당 JD에 대한 권한이 없습니다.", exception.getMessage());

        verify(toDoListRepository, never()).save(any(ToDoList.class));
    }

    @Test
    @DisplayName("실패: ToDoList가 카테고리별 제한 10개를 초과할 때 예외 발생")
    void createToDoList_fail_exceeds_category_limit() {
        // Given
        List<ToDoList> existingToDoLists = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            existingToDoLists.add(createTestToDoList((long) i, null, targetCategory, "ToDo " + i));
        }
        JD mockJd = createTestJd(jdId, mockMember, existingToDoLists);
        CreateToDoListRequestDto createToDoListDto = CreateToDoListRequestDto.builder().category(targetCategory).build();

        when(jdRepository.findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId())).thenReturn(Optional.of(mockJd));

        // When & Then
        ToDoListException exception = assertThrows(ToDoListException.class, () -> {
            toDoListService.createToDoList(jdId, createToDoListDto, mockMember);
        });

        assertEquals(ToDoListErrorCode.TODO_LIST_LIMIT_EXCEEDED_FOR_CATEGORY, exception.getErrorCode());
        assertEquals(ToDoListErrorCode.TODO_LIST_LIMIT_EXCEEDED_FOR_CATEGORY.getMessage(), exception.getMessage());

        verify(jdRepository, times(1)).findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId());
        verify(toDoListRepository, never()).save(any(ToDoList.class));
    }


    @Test
    @DisplayName("성공: 다른 카테고리의 ToDoList는 제한에 영향을 주지 않음")
    void createToDoList_success_other_category_does_not_affect() {
        // Given
        List<ToDoList> existingToDoLists = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            existingToDoLists.add(createTestToDoList((long) i, null, ToDoListType.CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL, "Other ToDo " + i));
        }
        JD mockJd = createTestJd(jdId, mockMember, existingToDoLists);
        CreateToDoListRequestDto createToDoListDto = CreateToDoListRequestDto.builder().category(targetCategory).build();

        when(jdRepository.findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId())).thenReturn(Optional.of(mockJd));
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

        assertDoesNotThrow(() -> {
            toDoListService.createToDoList(jdId, createToDoListDto, mockMember);
        });

        verify(jdRepository, times(1)).findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId());
        verify(toDoListRepository, times(1)).save(any(ToDoList.class));
    }

    @Test
    @DisplayName("ToDoList 성공적으로 수정")
    void updateToDoList_success() {
        // Given
        JD testJd = JD.builder()
                .id(jdId)
                .title(faker.lorem().sentence())
                .jdUrl(faker.internet().url())
                .companyName(faker.company().name())
                .job(faker.job().position())
                .content(faker.lorem().paragraph())
                .endedAt(faker.date().future(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay())
                .applyAt(null)
                .toDoLists(null)
                .member(mockMember)
                .memo(faker.lorem().sentence())
                .build();

        ToDoList originalToDoList = createTestToDoList(toDoListId, testJd, targetCategory, "Original Title");
        testJd.addToDoList(originalToDoList);
        UpdateToDoListRequestDto updateToDoListDto = UpdateToDoListRequestDto.builder()
                .title("새로운 ToDoList 제목")
                .category(targetCategory)
                .content("새로운 ToDoList 내용")
                .isDone(true)
                .build();

        when(jdRepository.findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId())).thenReturn(Optional.of(testJd));


        // When
        ToDoListResponseDto result = toDoListService.updateToDoList(jdId, toDoListId, updateToDoListDto, mockMember);

        // Then
        verify(jdRepository, times(1)).findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId());

        assertNotNull(result);
        assertEquals(toDoListId, result.getChecklist_id());
        assertEquals(updateToDoListDto.getCategory(), result.getCategory());
        assertEquals(updateToDoListDto.getTitle(), result.getTitle());
        assertEquals(updateToDoListDto.getContent(), result.getContent());
        assertEquals(updateToDoListDto.isDone(), result.isDone());
        assertEquals(jdId, result.getJdId());
    }

    @Test
    @DisplayName("ToDoList 완료 여부 수정 성공")
    void toggleComplete_success() {
        // Given
        JD testJd = JD.builder()
                .id(jdId)
                .title(faker.lorem().sentence())
                .jdUrl(faker.internet().url())
                .companyName(faker.company().name())
                .job(faker.job().position())
                .content(faker.lorem().paragraph())
                .endedAt(faker.date().future(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay())
                .applyAt(null)
                .toDoLists(null)
                .member(mockMember)
                .memo(faker.lorem().sentence())
                .build();

        ToDoList originalToDoList = createTestToDoList(toDoListId, testJd, targetCategory, "Original Title");
        testJd.addToDoList(originalToDoList);
        ToggleTodolistRequestDto dto = ToggleTodolistRequestDto.builder()
                .isDone(true)
                .build();
        when(jdRepository.findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId())).thenReturn(Optional.of(testJd));


        // When
        ToDoListResponseDto result = toDoListService.toggleComplete(jdId, toDoListId, dto, mockMember);

        // Then
        verify(jdRepository, times(1)).findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId());

        assertNotNull(result);
        assertEquals(toDoListId, result.getChecklist_id());
        assertEquals(originalToDoList.getCategory(), result.getCategory());
        assertEquals(originalToDoList.getTitle(), result.getTitle());
        assertEquals(originalToDoList.getContent(), result.getContent());
        assertEquals(dto.isDone(), result.isDone());
        assertEquals(jdId, result.getJdId());
    }


    @Test
    @DisplayName("ToDoList 성공적으로 조회")
    void getToDoList_success() {
        // Given
        JD testJd = JD.builder()
                .id(jdId)
                .title(faker.lorem().sentence())
                .jdUrl(faker.internet().url())
                .companyName(faker.company().name())
                .job(faker.job().position())
                .content(faker.lorem().paragraph())
                .endedAt(faker.date().future(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay())
                .applyAt(null)
                .toDoLists(null)
                .member(mockMember)
                .memo(faker.lorem().sentence())
                .build();
        ToDoList mockToDoList = createTestToDoList(toDoListId, null, targetCategory, "Test Title");
        testJd.addToDoList(mockToDoList);
        // Given
        when(jdRepository.findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId())).thenReturn(Optional.of(testJd));

        // When
        ToDoListResponseDto result = toDoListService.getToDoList(jdId, toDoListId, mockMember);

        // Then
        verify(jdRepository, times(1)).findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId());

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
        ToDoList mockToDoList = createTestToDoList(toDoListId, null, targetCategory, "ToDelete");
        JD testJd = createTestJd(jdId, mockMember, List.of(mockToDoList));

        when(jdRepository.findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId())).thenReturn(Optional.of(testJd));
        // When
        toDoListService.deleteToDoList(jdId, toDoListId, mockMember);

        // Then
        verify(jdRepository, times(1)).findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId());
        verify(toDoListRepository).delete(mockToDoList);
        assertThat(testJd.getToDoLists()).isEmpty();
    }

    @Test
    @DisplayName("업데이트 실패: ToDoList가 해당 JD에 속하지 않음 (조회 불가)")
    void updateToDoList_fail_toDoListNotFoundOrNotBelongToJd() {
        // Given
        JD mockJd = createTestJd(jdId, mockMember, Collections.emptyList());
        UpdateToDoListRequestDto updateToDoListDto = new UpdateToDoListRequestDto();
        when(jdRepository.findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId())).thenReturn(Optional.of(mockJd));

        // When & Then
        assertThrowsToDoListNotFound(() ->
                toDoListService.updateToDoList(jdId, toDoListId, updateToDoListDto, mockMember));

        verify(jdRepository, times(1)).findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId());
        verify(toDoListRepository, never()).save(any(ToDoList.class));
    }

    @Test
    @DisplayName("조회 실패: ToDoList가 해당 JD에 속하지 않음 (조회 불가)")
    void getToDoList_fail_toDoListNotFoundOrNotBelongToJd() {
        // Given
        JD mockJd = createTestJd(jdId, mockMember, Collections.emptyList());
        when(jdRepository.findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId())).thenReturn(Optional.of(mockJd));

        // When & Then
        assertThrowsToDoListNotFound(() ->
                toDoListService.getToDoList(jdId, toDoListId, mockMember));

        verify(jdRepository, times(1)).findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId());
    }

    @Test
    @DisplayName("삭제 실패: ToDoList가 해당 JD에 속하지 않음 (조회 불가)")
    void deleteToDoList_fail_toDoListNotFoundOrNotBelongToJd() {
        // Given
        JD mockJd = createTestJd(jdId, mockMember, Collections.emptyList());
        when(jdRepository.findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId())).thenReturn(Optional.of(mockJd));

        // When & Then
        assertThrowsToDoListNotFound(() ->
                toDoListService.deleteToDoList(jdId, toDoListId, mockMember));

        verify(jdRepository, times(1)).findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId());
        verify(toDoListRepository, never()).delete(any(ToDoList.class));
    }

    @Test
    @DisplayName("벌크 업데이트 실패: 생성/수정 ToDoList가 해당 JD에 속하지 않음 (조회 불가)")
    void bulkUpdateToDoLists_updateOrCreate_fail_toDoListNotFound() {
        // Given
        JD mockJd = createTestJd(jdId, mockMember, Collections.emptyList());
        when(jdRepository.findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId())).thenReturn(Optional.of(mockJd));
        ToDoListUpdateRequestDto updateToDoListUpdateReqDto = ToDoListUpdateRequestDto.builder()
                .id(toDoListId) // 존재하지 않는 ID
                .title("some title")
                .content("some content")
                .build();

        BulkToDoListUpdateRequestDto bulkReqWrongJd = BulkToDoListUpdateRequestDto.builder()
                .category(targetCategory)
                .updatedOrCreateToDoLists(Collections.singletonList(updateToDoListUpdateReqDto))
                .deletedToDoListIds(Collections.emptyList())
                .build();
        // When & Then
        assertThrowsToDoListNotFound(() ->
                toDoListService.bulkUpdateToDoLists(jdId, bulkReqWrongJd, mockMember));

        verify(jdRepository, times(1)).findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId());
        verify(toDoListRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("벌크 업데이트 실패: 삭제할 ToDoList가 해당 JD에 속하지 않음")
    void bulkUpdateToDoLists_delete_fail_toDoListNotBelongToJd() {
        // Given
        JD mockJd = createTestJd(jdId, mockMember, Collections.emptyList());
        when(jdRepository.findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId())).thenReturn(Optional.of(mockJd));

        List<Long> deletedIds = Collections.singletonList(toDoListId); // some random ID
        BulkToDoListUpdateRequestDto bulkReqDeleteWrongJd = BulkToDoListUpdateRequestDto.builder()
                .category(targetCategory)
                .updatedOrCreateToDoLists(Collections.emptyList())
                .deletedToDoListIds(deletedIds)
                .build();

        // When & Then
        assertThrowsToDoListNotBelongToJd(() ->
                toDoListService.bulkUpdateToDoLists(jdId, bulkReqDeleteWrongJd, mockMember));

        verify(jdRepository, times(1)).findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId());
        verify(toDoListRepository, never()).deleteAllInBatch(anyList());
    }

    @Test
    @DisplayName("Bulk Update: 성공적으로 여러 ToDoList 생성, 수정, 삭제")
    void bulkUpdateToDoLists_success() {
        // Given
        JD testJd = JD.builder()
                .id(jdId)
                .title(faker.lorem().sentence())
                .jdUrl(faker.internet().url())
                .companyName(faker.company().name())
                .job(faker.job().position())
                .content(faker.lorem().paragraph())
                .endedAt(faker.date().future(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay())
                .applyAt(null)
                .toDoLists(null)
                .member(mockMember)
                .memo(faker.lorem().sentence())
                .build();

        ToDoListUpdateRequestDto newToDoListDto = ToDoListUpdateRequestDto.builder()
                .id(null)
                .title(faker.lorem().sentence())
                .content(faker.lorem().paragraph())
                .isDone(faker.bool().bool())
                .build();

        Long anotherExistingId = 102L;
        ToDoList existingToDoListForUpdate = ToDoList.builder()
                .id(anotherExistingId)
                .title("기존 제목")
                .content("기존 내용")
                .memo("기존 메모")
                .isDone(false)
                .category(targetCategory)
                .jd(testJd)
                .build();
        testJd.addToDoList(existingToDoListForUpdate);
        ToDoListUpdateRequestDto updateAnotherDto = ToDoListUpdateRequestDto.builder()
                .id(anotherExistingId)
                .title("Updated Another Title")
                .content("Updated Another Content")
                .isDone(true)
                .build();

        Long deletedToDoListId = 103L;
        ToDoList toDoListToDelete = ToDoList.builder()
                .id(deletedToDoListId)
                .title("삭제될 제목")
                .content("삭제될 내용")
                .memo("삭제될 메모")
                .isDone(false)
                .category(targetCategory)
                .jd(testJd)
                .build();
        testJd.addToDoList(toDoListToDelete);

        BulkToDoListUpdateRequestDto request = BulkToDoListUpdateRequestDto.builder()
                .category(targetCategory)
                .updatedOrCreateToDoLists(Arrays.asList(newToDoListDto, updateAnotherDto))
                .deletedToDoListIds(Collections.singletonList(deletedToDoListId))
                .build();

        when(jdRepository.findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId())).thenReturn(Optional.of(testJd));
        when(toDoListRepository.findToDoListWithJdByIdAndJdId(anotherExistingId, jdId)).thenReturn(Optional.of(existingToDoListForUpdate));
        when(toDoListRepository.findAllByIdsWithJd(Collections.singletonList(deletedToDoListId))).thenReturn(Collections.singletonList(toDoListToDelete));

        // When
        toDoListService.bulkUpdateToDoLists(jdId, request, mockMember);

        // Then
        verify(toDoListRepository, times(2)).saveAll(toDoListListCaptor.capture());
        verify(toDoListRepository).deleteAllById(idListCaptor.capture());

        List<List<ToDoList>> allSavedLists = toDoListListCaptor.getAllValues();
        List<ToDoList> createdLists = allSavedLists.get(0);
        List<ToDoList> updatedLists = allSavedLists.get(1);
        List<Long> deletedIds = idListCaptor.getValue();

        // 생성 검증
        assertThat(createdLists).hasSize(1);
        assertThat(createdLists.get(0).getTitle()).isEqualTo(newToDoListDto.getTitle());

        // 수정 검증
        assertThat(updatedLists).hasSize(1);
        assertThat(updatedLists.get(0).getId()).isEqualTo(anotherExistingId);
        assertThat(existingToDoListForUpdate.getTitle()).isEqualTo(updateAnotherDto.getTitle());
        assertThat(existingToDoListForUpdate.isDone()).isTrue();

        // 삭제 검증
        assertThat(deletedIds).hasSize(1);
        assertThat(deletedIds.get(0)).isEqualTo(deletedToDoListId);
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

        // JD 조회는 성공해야 함
        when(jdRepository.findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId()))
                .thenReturn(Optional.of(createTestJd(jdId, mockMember, Collections.emptyList())));

        // When & Then
        ToDoListException exception = assertThrows(ToDoListException.class,
                () -> toDoListService.bulkUpdateToDoLists(jdId, request, mockMember));
        assertEquals(ToDoListErrorCode.CATEGORY_REQUIRED, exception.getErrorCode());

        verify(toDoListRepository, never()).saveAll(anyList());
        verify(toDoListRepository, never()).deleteAllInBatch(anyList());
    }

    @Test
    @DisplayName("Bulk Update: 업데이트/삭제 시 ToDoList가 해당 JD/카테고리에 속하지 않으면 실패")
    void bulkUpdateToDoLists_fail_notBelongToJdOrCategory() {
        // Given
        Long deletedIdWrongOwner = 200L;

        BulkToDoListUpdateRequestDto requestDeleteWrongOwner = BulkToDoListUpdateRequestDto.builder()
                .category(targetCategory)
                .updatedOrCreateToDoLists(Collections.emptyList())
                .deletedToDoListIds(Collections.singletonList(deletedIdWrongOwner))
                .build();

        JD mockJd = createTestJd(jdId, mockMember, Collections.emptyList());
        when(jdRepository.findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId())).thenReturn(Optional.of(mockJd));

        // When & Then
        assertThrowsToDoListNotBelongToJd(() -> toDoListService.bulkUpdateToDoLists(jdId, requestDeleteWrongOwner, mockMember));

        // Verify
        verify(toDoListRepository, never()).saveAll(anyList());
        verify(toDoListRepository, never()).deleteAllInBatch(anyList());
    }


    @Test
    @DisplayName("Get ToDoLists By Category: 성공적으로 조회")
    void getToDoListsByJdAndCategory_success() {
        // Given
        JD mockJd = createTestJd(jdId, mockMember, Collections.emptyList());
        ToDoList toDoList1 = createTestToDoList(1L, mockJd, targetCategory, "ToDo 1");
        ToDoList toDoList2 = createTestToDoList(2L, mockJd, targetCategory, "ToDo 2");
        List<ToDoList> mockToDoLists = List.of(toDoList1, toDoList2);

        when(jdRepository.findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId())).thenReturn(Optional.of(mockJd));
        when(toDoListRepository.findToDoListsByJdIdAndCategoryWithJd(jdId, targetCategory)).thenReturn(mockToDoLists);

        // When
        ToDoListGetByCategoryResponseDto result = toDoListService.getToDoListsByJdAndCategory(jdId, targetCategory, mockMember);

        // Then
        verify(jdRepository, times(1)).findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId());
        verify(toDoListRepository, times(1)).findToDoListsByJdIdAndCategoryWithJd(jdId, targetCategory);

        assertNotNull(result);
        assertEquals(jdId, result.getJdId());
        assertEquals(targetCategory, result.getCategory());

        assertThat(result.getToDoLists()).hasSize(2)
                .extracting(ToDoListResponseDto::getTitle)
                .containsExactlyInAnyOrder("ToDo 1", "ToDo 2");
    }

    @Test
    @DisplayName("Get ToDoLists By Category: JD를 찾을 수 없을 때 실패")
    void getToDoListsByJdAndCategory_fail_jdNotFound() {
        // Given
        when(jdRepository.findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThrowsUnauthorizedAccess(() -> toDoListService.getToDoListsByJdAndCategory(jdId, targetCategory, mockMember));

        verify(toDoListRepository, never()).findToDoListsByJdIdAndCategoryWithJd(jdId, targetCategory);
    }

    @Test
    @DisplayName("Get ToDoLists By Category: 해당 카테고리에 ToDoList가 없을 때 빈 리스트 반환")
    void getToDoListsByJdAndCategory_success_emptyList() {
        // Given
        JD mockJd = createTestJd(jdId, mockMember, Collections.emptyList());
        when(jdRepository.findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId())).thenReturn(Optional.of(mockJd));
        when(toDoListRepository.findToDoListsByJdIdAndCategoryWithJd(jdId, targetCategory)).thenReturn(Collections.emptyList());

        // When
        ToDoListGetByCategoryResponseDto result = toDoListService.getToDoListsByJdAndCategory(jdId, targetCategory, mockMember);

        // Then
        verify(jdRepository, times(1)).findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId());
        verify(toDoListRepository, times(1)).findToDoListsByJdIdAndCategoryWithJd(jdId, targetCategory);

        assertNotNull(result);
        assertEquals(jdId, result.getJdId());
        assertEquals(targetCategory, result.getCategory());
        assertNotNull(result.getToDoLists());
        assertTrue(result.getToDoLists().isEmpty());
    }

    @Test
    @DisplayName("ToDoList 여러 개 완료 여부 업데이트 성공")
    void updateIsDoneTodoLists_success() {
        // Given
        JD testJd = createTestJd(jdId, mockMember, new ArrayList<>());

        ToDoList todo1 = createTestToDoList(201L, testJd, targetCategory, "Todo 1");
        ToDoList todo2 = createTestToDoList(202L, testJd, targetCategory, "Todo 2");
        testJd.addToDoList(todo1);
        testJd.addToDoList(todo2);

        List<Long> todoIds = Arrays.asList(201L, 202L);
        UpdateTodoListsIsDoneRequestDto requestDto = UpdateTodoListsIsDoneRequestDto.builder()
                .toDoListIds(todoIds)
                .isDone(true)
                .build();

        when(jdRepository.findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId()))
                .thenReturn(Optional.of(testJd));
        when(toDoListRepository.findAllById(todoIds)).thenReturn(Arrays.asList(todo1, todo2));

        // When
        UpdateIsDoneTodoListsResponseDto result = toDoListService.updateIsDoneTodoLists(jdId, requestDto, mockMember);

        // Then
        verify(jdRepository, times(1)).findJdWithMemberAndToDoListsByIdAndMemberId(jdId, mockMember.getId());
        verify(toDoListRepository, times(1)).findAllById(todoIds);

        assertThat(result.getToDoLists()).hasSize(2)
                .extracting(ToDoListResponseDto::getChecklist_id)
                .containsExactlyInAnyOrder(201L, 202L);
        assertThat(result.getToDoLists()).allMatch(ToDoListResponseDto::isDone);

        assertTrue(result.isDone());
    }

    @Test
    @DisplayName("ToDoList 업데이트 시 - 존재하지 않는 ID는 무시된다")
    void updateIsDoneTodoLists_ignoreNotFoundIds() {
        // given
        Long jdId = 1L;
        Member member = createTestMember();
        UpdateTodoListsIsDoneRequestDto dto = new UpdateTodoListsIsDoneRequestDto(List.of(999L), true);

        // mock: 없는 ID라 빈 리스트 반환
        when(toDoListRepository.findAllById(dto.getToDoListIds()))
                .thenReturn(Collections.emptyList());

        // when
        UpdateIsDoneTodoListsResponseDto response =
                toDoListService.updateIsDoneTodoLists(jdId, dto, member);

        // then
        assertThat(response.getToDoLists()).isEmpty(); // 응답에도 업데이트된 리스트 없음
        assertThat(response.isDone()).isTrue();
    }

    private Member createTestMember() {
        return Member.builder()
                .id(1L)
                .email("test@example.com")
                .name("테스트유저")
                .nickname("tester")
                .build();
    }
    private JD createTestJd(Long jdId, Member member, List<ToDoList> toDoLists) {
        return JD.builder()
                .id(jdId)
                .title(faker.lorem().sentence())
                .jdUrl(faker.internet().url())
                .companyName(faker.company().name())
                .job(faker.job().position())
                .content(faker.lorem().paragraph())
                .endedAt(faker.date().future(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay())
                .applyAt(null)
                .toDoLists(new ArrayList<>(toDoLists))
                .member(member)
                .memo(faker.lorem().sentence())
                .build();
    }

    private ToDoList createTestToDoList(Long id, JD jd, ToDoListType category, String title) {
        return ToDoList.builder().id(id).jd(jd).category(category).title(title)
                .content(faker.lorem().paragraph()).memo("").isDone(false).build();
    }


    private void assertThrowsUnauthorizedAccess(Executable executable) {
        JDException exception = assertThrows(JDException.class, executable);
        assertEquals(JDErrorCode.UNAUTHORIZED_ACCESS, exception.getErrorCode());
        assertEquals("해당 JD에 대한 권한이 없습니다.", exception.getMessage());
    }

    private void assertThrowsToDoListNotFound(Executable executable) {
        ToDoListException exception = assertThrows(ToDoListException.class, executable);
        assertEquals(ToDoListErrorCode.UNAUTHORIZED_ACCESS, exception.getErrorCode());
        assertEquals("해당 투두리스트 대한 권한이 없습니다.", exception.getMessage());
    }

    private void assertThrowsToDoListNotBelongToJd(Executable executable) {
        ToDoListException exception = assertThrows(ToDoListException.class, executable);
        assertEquals(ToDoListErrorCode.TODO_LIST_NOT_BELONG_TO_JD, exception.getErrorCode());
        assertEquals("해당 JD에 속하지 않는 ToDoList입니다.", exception.getMessage());
    }

}
