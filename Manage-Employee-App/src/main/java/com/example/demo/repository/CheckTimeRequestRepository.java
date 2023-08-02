package com.example.demo.repository;

import com.example.demo.entity.CheckTimeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckTimeRequestRepository extends JpaRepository<CheckTimeRequest, Long> {
    CheckTimeRequest findByUserID(Long userID);
}
