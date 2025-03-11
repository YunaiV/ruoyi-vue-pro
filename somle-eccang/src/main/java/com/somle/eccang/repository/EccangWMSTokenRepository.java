package com.somle.eccang.repository;

import com.somle.eccang.model.EccangWMSToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EccangWMSTokenRepository extends JpaRepository<EccangWMSToken, String> {
}
