package com.springboot.BankingSystem.repository;

import com.springboot.BankingSystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("""
select u
from User u
where u.username = ?1 AND u.isActive = ?2
""")
    User loadUserByUsername(String username, boolean isActive);
}
