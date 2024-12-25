package com.somle.home24.repository;

import com.somle.home24.model.pojo.Home24Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Home24AccountRepository extends JpaRepository<Home24Account, Long> {
}
