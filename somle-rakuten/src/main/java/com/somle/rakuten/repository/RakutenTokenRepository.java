package com.somle.rakuten.repository;

import com.somle.rakuten.model.pojo.RakutenTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RakutenTokenRepository extends JpaRepository<RakutenTokenEntity, Integer> {
}
