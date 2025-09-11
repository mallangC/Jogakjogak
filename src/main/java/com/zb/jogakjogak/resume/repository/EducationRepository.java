package com.zb.jogakjogak.resume.repository;

import com.zb.jogakjogak.resume.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {
}
