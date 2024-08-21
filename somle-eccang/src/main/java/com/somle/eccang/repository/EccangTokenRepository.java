package com.somle.eccang.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.somle.eccang.model.EccangToken;
import org.springframework.stereotype.Repository;

@Repository
public interface EccangTokenRepository extends JpaRepository<EccangToken, Long> {
}
