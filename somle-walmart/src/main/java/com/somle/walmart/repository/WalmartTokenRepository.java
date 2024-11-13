package com.somle.walmart.repository;

import com.somle.walmart.model.WalmartToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalmartTokenRepository extends JpaRepository<WalmartToken, String> {
}

