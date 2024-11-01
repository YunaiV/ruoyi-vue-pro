package com.somle.microsoft.repository;

import com.somle.microsoft.model.MicrosoftClient;
import com.somle.microsoft.model.PowerbiAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PowerbiAccountRepository extends JpaRepository<PowerbiAccount, String> {
}
