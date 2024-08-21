package com.somle.esb.repository;

import com.somle.esb.model.AliyunToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AliyunTokenRepository extends JpaRepository<AliyunToken, String> {
}