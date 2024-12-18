package com.somle.otto.repository;

import com.somle.otto.model.OttoAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OttoAccountRepository extends JpaRepository<OttoAccount, Long> {
}
