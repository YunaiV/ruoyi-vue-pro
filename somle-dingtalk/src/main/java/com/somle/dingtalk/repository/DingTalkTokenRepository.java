package com.somle.dingtalk.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.somle.dingtalk.model.DingTalkToken;
import org.springframework.stereotype.Repository;

@Repository
public interface DingTalkTokenRepository extends JpaRepository<DingTalkToken, Long> {
}
