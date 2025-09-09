package com.zb.jogakjogak.resume.repository;

import com.zb.jogakjogak.resume.entity.SkillWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillWordRepository extends JpaRepository<SkillWord, Long> {
    @Query(value = "SELECT * FROM skill_word " +
                   "WHERE MATCH(content) AGAINST(?1 IN BOOLEAN MODE)", nativeQuery = true)
    List<SkillWord> findByContentMatchAgainst(String query);
}
