package com.zb.jogakjogak.jobDescription.repository;

import com.zb.jogakjogak.jobDescription.entity.JD;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JDRepository extends JpaRepository<JD, Long> {

}
