package com.somle.shopee.repository;

import com.somle.shopee.model.ShopeeAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopeeAccountRepository extends JpaRepository<ShopeeAccount, Long> {
}

