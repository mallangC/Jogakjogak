package com.zb.jogakjogak.jobDescription.service;

import com.zb.jogakjogak.global.exception.JDErrorCode;
import com.zb.jogakjogak.global.exception.JDException;
import com.zb.jogakjogak.jobDescription.domain.requestDto.ToDoListDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListResponseDto;
import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.jobDescription.entity.ToDoList;
import com.zb.jogakjogak.jobDescription.repsitory.ToDoListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ToDoListService {

    private final ToDoListRepository toDoListRepository;

    /**
     * 특정 JD에 새로운 ToDoList를 추가하는 메서드
     *
     * @param jdId        ToDoList를 추가할 JD의 ID
     * @param toDoListDto 추가할 ToDoList의 정보
     * @return 새로 생성된 ToDoList의 응답 DTO
     */
    @Transactional // 하나의 트랜잭션으로 묶어 원자성을 보장
    public ToDoListResponseDto createToDoList(Long jdId, ToDoListDto toDoListDto) {

        JD jd = toDoListRepository.findById(jdId)
                .orElseThrow(() -> new JDException(JDErrorCode.JD_NOT_FOUND)).getJd();

        ToDoList toDoList = ToDoList.fromDto(toDoListDto, jd);
        jd.addToDoList(toDoList);
        ToDoList savedToDoList = toDoListRepository.save(toDoList);
        return ToDoListResponseDto.fromEntity(savedToDoList);
    }
}
