package com.somle.kingdee.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.somle.kingdee.model.KingdeeToken;

public interface KingdeeTokenRepository extends JpaRepository<KingdeeToken, Long> {
    public KingdeeToken findByAccountName(String name);
    public KingdeeToken findByOuterInstanceId(String outerInstanceId);
}