package com.somle.wayfair.repository;

import com.somle.wayfair.model.WayfairToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WayfairTokenRepository extends JpaRepository<WayfairToken, String> {
}

