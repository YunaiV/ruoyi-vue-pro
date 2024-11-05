package com.somle.wangdian.repository;

import com.somle.wangdian.model.WangdianToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WangdianTokenRepository extends JpaRepository<WangdianToken, String> {
}

