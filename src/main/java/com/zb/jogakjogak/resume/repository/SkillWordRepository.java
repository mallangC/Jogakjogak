package com.zb.jogakjogak.resume.repository;

import com.zb.jogakjogak.resume.entity.SkillWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillWordRepository extends JpaRepository<SkillWord, Long> {
    @Query(value = "SELECT sw.content FROM skill_word sw WHERE sw.content LIKE CONCAT(?1, '%')", nativeQuery = true)
    List<String> findContentsByContentLike(String query);
}
