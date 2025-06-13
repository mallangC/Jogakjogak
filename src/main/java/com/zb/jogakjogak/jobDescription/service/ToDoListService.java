package com.zb.jogakjogak.jobDescription.service;

import com.zb.jogakjogak.global.exception.JDErrorCode;
import com.zb.jogakjogak.global.exception.JDException;
import com.zb.jogakjogak.global.exception.ToDoListErrorCode;
import com.zb.jogakjogak.global.exception.ToDoListException;
import com.zb.jogakjogak.jobDescription.domain.requestDto.ToDoListDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListDeleteResponseDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListResponseDto;
import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.jobDescription.entity.ToDoList;
import com.zb.jogakjogak.jobDescription.repository.JDRepository;
import com.zb.jogakjogak.jobDescription.repository.ToDoListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ToDoListService {

    private final JDRepository jdRepository;
    private final ToDoListRepository toDoListRepository;

    /**
     * 특정 JD에 새로운 ToDoList를 추가하는 메서드
     *
     * @param jdId        ToDoList를 추가할 JD의 ID
     * @param toDoListDto 추가할 ToDoList의 정보
     * @return 새로 생성된 ToDoList의 응답 DTO
     */
    @Transactional
    public ToDoListResponseDto createToDoList(Long jdId, ToDoListDto toDoListDto) {
        JD jd = findJdById(jdId);
        ToDoList toDoList = ToDoList.fromDto(toDoListDto, jd);
        jd.addToDoList(toDoList);
        ToDoList savedToDoList = toDoListRepository.save(toDoList);
        return ToDoListResponseDto.fromEntity(savedToDoList);
    }

    /**
     * 특정 JD에 속한 ToDoList를 수정하는 메서드
     *
     * @param jdId        ToDoList가 속한 JD의 ID
     * @param toDoListId  수정할 ToDoList의 ID
     * @param toDoListDto 업데이트할 ToDoList의 정보
     * @return 수정된 ToDoList의 응답 DTO
     */
    @Transactional
    public ToDoListResponseDto updateToDoList(Long jdId, Long toDoListId, ToDoListDto toDoListDto) {
        ToDoList toDoList = findToDoListAndValidateOwnership(jdId, toDoListId);
        toDoList.updateFromDto(toDoListDto);
        ToDoList updatedToDoList = toDoListRepository.save(toDoList);
        return ToDoListResponseDto.fromEntity(updatedToDoList);
    }

    /**
     * 특정 JD에 속한 ToDoList를 조회하는 메서드
     *
     * @param jdId ToDoList가 속한 JD의 ID
     * @param toDoListId 조회할 ToDoList의 ID
     * @return 조회된 ToDoList의 응답 DTO
     */
    public ToDoListResponseDto getToDoList(Long jdId, Long toDoListId) {
        ToDoList toDoList = findToDoListAndValidateOwnership(jdId, toDoListId);
        return ToDoListResponseDto.fromEntity(toDoList);
    }

    /**
     * 특정 JD에 속한 ToDoList를 삭제하는 메서드
     *
     * @param jdId ToDoList가 속한 JD의 ID
     * @param toDoListId 조회할 ToDoList의 ID
     * @return 삭제된 ToDoList의 응답 DTO
     */
    public ToDoListDeleteResponseDto deleteToDoList(Long jdId, Long toDoListId) {

        ToDoList toDoList = findToDoListAndValidateOwnership(jdId, toDoListId);

        toDoListRepository.delete(toDoList);
        return ToDoListDeleteResponseDto.builder()
                .checklist_id(toDoList.getId())
                .build();
    }

    /**
     * ID로 JD를 찾고, 없으면 예외를 발생시킵니다.
     * Optional을 사용하여 null 체크 대신 존재 여부를 명확히 처리합니다.
     * @param jdId 찾을 JD의 ID
     * @return 찾아진 JD 엔티티
     * @throws JDException JD를 찾을 수 없을 경우
     */
    private JD findJdById(Long jdId) {
        return jdRepository.findById(jdId)
                .orElseThrow(() -> new JDException(JDErrorCode.JD_NOT_FOUND));
    }

    /**
     * ToDoList를 ID로 찾고, 해당 JD에 속하는지 검증합니다.
     * Optional 체이닝을 활용하여 가독성을 높이고 null 체크를 줄입니다.
     * @param jdId 검증할 JD의 ID
     * @param toDoListId 찾을 ToDoList의 ID
     * @return 찾아진 ToDoList 엔티티
     * @throws ToDoListException ToDoList를 찾을 수 없거나 해당 JD에 속하지 않을 경우
     */
    private ToDoList findToDoListAndValidateOwnership(Long jdId, Long toDoListId) {
        findJdById(jdId);

        ToDoList toDoList = toDoListRepository.findById(toDoListId)
                .orElseThrow(() -> new ToDoListException(ToDoListErrorCode.TODO_LIST_NOT_FOUND));

        if (!toDoList.getJd().getId().equals(jdId)) {
            throw new ToDoListException(ToDoListErrorCode.TODO_LIST_NOT_BELONG_TO_JD);
        }
        return toDoList;
    }

}
