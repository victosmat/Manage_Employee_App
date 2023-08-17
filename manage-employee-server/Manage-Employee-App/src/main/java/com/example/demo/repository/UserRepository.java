package com.example.demo.repository;

import com.example.demo.entity.Account;
import com.example.demo.entity.User;
import com.example.demo.payLoad.dto.IUserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    User findByEmail(String email);

    User findByAccount(Account account);

    @Query(value = "SELECT u FROM User u")
    List<IUserDTO> findAllUsers(Sort sort);

    @Query(value = "SELECT u FROM User u")
    Page<User> findAllByPage(Pageable pageable);

    @Query(value = "SELECT u FROM User u")
    Slice<User> findAllBySlice(Pageable pageable);
}
