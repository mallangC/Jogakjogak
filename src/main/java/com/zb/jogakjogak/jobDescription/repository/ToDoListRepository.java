package com.zb.jogakjogak.jobDescription.repository;


import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.jobDescription.entity.ToDoList;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToDoListRepository extends JpaRepository<ToDoList, Long> {

    List<ToDoList> findByJdAndCategory(JD jd, ToDoListType targetCategory);

    List<ToDoList> findByJdId(long jdId);
    Object findByIdAndJdId(Long toDoListId, Long jdId);
}
