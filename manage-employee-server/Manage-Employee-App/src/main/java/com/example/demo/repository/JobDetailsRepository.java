package com.example.demo.repository;

import com.example.demo.entity.JobDetails;
import com.example.demo.entity.User;
import com.example.demo.payLoad.dto.TotalJobByUser;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface JobDetailsRepository extends JpaRepository<JobDetails, Long> {
    @Query(value = "SELECT j FROM JobDetails j")
    List<JobDetails> findAllJobDetails(Sort sort);

    @Query("SELECT new com.example.demo.payLoad.dto.TotalJobByUser(u.ID, COUNT(j.jobCode)) " +
            "FROM JobDetails j JOIN j.users u GROUP BY u.ID")
    List<TotalJobByUser> getTotalJobByUser();

    JobDetails findByJobCode(String jobCode);

    @Query("SELECT j FROM JobDetails j JOIN j.users u WHERE u.ID = ?1")
    List<JobDetails> findAllJobDetailsByUser(Long userID);
}
