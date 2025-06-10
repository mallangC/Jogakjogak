package com.zb.jogakjogak.jobDescription.repsitory;

import com.zb.jogakjogak.jobDescription.entity.ToDoList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ToDoListRepository extends JpaRepository<ToDoList, Long> {

}
