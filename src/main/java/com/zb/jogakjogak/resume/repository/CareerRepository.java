package com.zb.jogakjogak.resume.repository;

import com.zb.jogakjogak.resume.entity.Career;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CareerRepository extends JpaRepository<Career, Long> {
}
