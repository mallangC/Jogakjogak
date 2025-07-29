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
public interface ToDoListRepository extends JpaRepository<ToDoList, Long>, ToDoListRepositoryCustom {

    //테스트용
    List<ToDoList> findAllByJdId(Long jdId);
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

    Integer countByJd_Id(Long id);
}
