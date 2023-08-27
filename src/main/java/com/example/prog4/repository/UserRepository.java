package com.example.prog4.repository;

import com.example.prog4.repository.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    @Query(value = "select * from \"user\" u where u.username = :username limit 1", nativeQuery = true)
    Optional<User> findOneByUsername(@Param("username") String username);
}
