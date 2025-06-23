package com.zb.jogakjogak.jobDescription.repository;


import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.jobDescription.entity.ToDoList;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ToDoListRepository extends JpaRepository<ToDoList, Long> {

    List<ToDoList> findByJdAndCategory(JD jd, ToDoListType targetCategory);

    List<ToDoList> findByJdId(long jdId);

    @Query("SELECT t FROM ToDoList t JOIN FETCH t.jd j WHERE j.id = :jdId AND t.category = :category")
    List<ToDoList> findByJdIdAndCategoryFetch(@Param("jdId") Long jdId, @Param("category") ToDoListType category);

    @Query("SELECT t FROM ToDoList t JOIN FETCH t.jd WHERE t.id = :id")
    Optional<ToDoList> findByIdWithJd(@Param("id") Long id);
}
