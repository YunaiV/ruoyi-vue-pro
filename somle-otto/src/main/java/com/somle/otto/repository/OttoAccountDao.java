package com.somle.otto.repository;

import com.somle.otto.model.pojo.OttoAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OttoAccountDao extends JpaRepository<OttoAccount, Long> {
}
