package com.kafka.consumer.repository;

import com.kafka.consumer.entity.UserInSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserInSession, Long> {
    boolean existsByUsername(String username);
}
