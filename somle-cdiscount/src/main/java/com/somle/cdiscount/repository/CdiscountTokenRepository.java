package com.somle.cdiscount.repository;

import com.somle.cdiscount.model.CdiscountToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CdiscountTokenRepository extends JpaRepository<CdiscountToken, String> {
}
