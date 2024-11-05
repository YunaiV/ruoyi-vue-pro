package com.wangdian.repository;

import com.wangdian.model.WangdianToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WangdianTokenRepository extends JpaRepository<WangdianToken, String> {
}

