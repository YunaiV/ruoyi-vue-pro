package com.somle.autonomous.repository;

import com.somle.autonomous.model.AutonomousAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutonomousAccountRepository extends JpaRepository<AutonomousAccount, Long> {
}
