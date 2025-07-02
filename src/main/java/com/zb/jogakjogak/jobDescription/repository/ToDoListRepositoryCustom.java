package com.zb.jogakjogak.jobDescription.repository;

import com.zb.jogakjogak.jobDescription.entity.ToDoList;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;

import java.util.List;
import java.util.Optional;

public interface ToDoListRepositoryCustom {
    /**
     * ToDoList ID와 JD ID를 기반으로 ToDoList와 연관된 JD를 즉시 로딩하여 조회합니다.
     * 이 메서드는 ToDoList의 소유권 검증을 쿼리 내에서 수행하며, N+1 문제를 방지합니다.
     *
     * @param toDoListId 조회할 ToDoList의 ID
     * @param jdId       ToDoList가 속한 JD의 ID
     * @return 조회된 ToDoList 객체 (Optional)
     */
    Optional<ToDoList> findToDoListWithJdByIdAndJdId(Long toDoListId, Long jdId);

    /**
     * 특정 JD에 속한 특정 카테고리의 ToDoList 목록을 조회합니다.
     * JD를 즉시 로딩하여 N+1 문제를 방지합니다.
     *
     * @param jdId     ToDoList가 속한 JD의 ID
     * @param category 조회할 ToDoList의 카테고리
     * @return 조건에 맞는 ToDoList 목록
     */
    List<ToDoList> findToDoListsByJdIdAndCategoryWithJd(Long jdId, ToDoListType category);

    /**
     * 특정 JD에 속한 특정 카테고리의 ToDoList 개수를 조회합니다.
     *
     * @param jdId     JD의 ID
     * @param category ToDoList의 카테고리
     * @return 해당 조건에 맞는 ToDoList의 개수
     */
    long countToDoListsByJdIdAndCategory(Long jdId, ToDoListType category);

    /**
     * 특정 JD에 속한 ToDoList 중 완료된 항목의 개수를 조회합니다.
     *
     * @param jdId JD의 ID
     * @return 완료된 ToDoList의 개수
     */
    long countDoneToDoListsByJdId(Long jdId);

    /**
     * 특정 JD에 속한 ToDoList 중 완료되지 않은 항목의 개수를 조회합니다.
     *
     * @param jdId JD의 ID
     * @return 완료되지 않은 ToDoList의 개수
     */
    long countUndoneToDoListsByJdId(Long jdId);

    /**
     * 주어진 ID 목록에 해당하는 ToDoList들을 조회하면서, 연관된 JD를 즉시 로딩합니다.
     *
     * @param ids 조회할 ToDoList ID 목록
     * @return 조회된 ToDoList 목록
     */
    List<ToDoList> findAllByIdsWithJd(List<Long> ids);
}
