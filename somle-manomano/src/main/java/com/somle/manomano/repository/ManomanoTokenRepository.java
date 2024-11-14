package com.somle.manomano.repository;

import com.somle.manomano.model.ManomanoToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManomanoTokenRepository extends JpaRepository<ManomanoToken, Long> {
}

