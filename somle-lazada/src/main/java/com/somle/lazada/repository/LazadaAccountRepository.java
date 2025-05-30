package com.somle.lazada.repository;

import com.somle.lazada.model.LazadaAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LazadaAccountRepository extends JpaRepository<LazadaAccount, Long> {
}

