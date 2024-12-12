package com.somle.rakuten.repository;

import com.somle.rakuten.model.pojo.RakutenTokenEntityDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RakutenTokenRepository extends JpaRepository<RakutenTokenEntityDO, Integer> {
}
