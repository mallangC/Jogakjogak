package com.zb.jogakjogak.jobDescription.service;

import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListDto;
import com.zb.jogakjogak.jobDescription.entity.ToDoList;
import com.zb.jogakjogak.jobDescription.repsitory.ToDoListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ToDoListService {

    private final ToDoListRepository toDoListRepository;

    public List<ToDoList> saveToDoList(List<ToDoListDto> toDoList) {
        List<ToDoList> entities = toDoList.stream()
                .map(ToDoList::fromDto)
                .collect(Collectors.toList());

        return toDoListRepository.saveAll(entities);
    }
}
