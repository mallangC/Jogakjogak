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

    List<ToDoList> findByJdId(Long jdId);

    List<ToDoList> findAllByJdId(Long jdId);

    @Query("SELECT t FROM ToDoList t JOIN FETCH t.jd j WHERE j.id = :jdId AND t.category = :category")
    List<ToDoList> findByJdIdAndCategoryFetch(@Param("jdId") Long jdId, @Param("category") ToDoListType category);

    @Query("SELECT t FROM ToDoList t JOIN FETCH t.jd WHERE t.id = :id")
    Optional<ToDoList> findByIdWithJd(@Param("id") Long id);

    long countByJdIdAndCategory(Long id, ToDoListType targetCategory);
    /*
    @Query("SELECT COUNT(t) FROM toDoList t JOIN t.jd j WHERE t.isDone = true AND j.Id = :jdId")
    Integer countByIsDoneTrueAndJd_JdId(@Param("jdId") Long jdId);
    */
    Integer countByIsDoneTrueAndJd_Id(Long jdId);

    /*
    @Query("SELECT COUNT(t) FROM toDoList t JOIN t.jd j WHERE t.isDone = false AND j.Id = :jdId")
    Integer countByIsDoneFalseAndJd_JdId(@Param("jdId") Long jdId);
    */
    Integer countByIsDoneFalseAndJd_Id(Long jdId);

}
