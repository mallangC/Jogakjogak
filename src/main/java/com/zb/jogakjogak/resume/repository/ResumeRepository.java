package com.zb.jogakjogak.resume.repository;

import com.zb.jogakjogak.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Integer> {

}
