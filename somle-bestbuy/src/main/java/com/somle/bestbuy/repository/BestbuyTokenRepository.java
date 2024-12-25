package com.somle.bestbuy.repository;

import com.somle.bestbuy.model.BestbuyToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BestbuyTokenRepository extends JpaRepository<BestbuyToken, String> {
}