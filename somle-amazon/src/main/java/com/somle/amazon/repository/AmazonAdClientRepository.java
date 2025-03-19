package com.somle.amazon.repository;

import com.somle.amazon.model.AmazonAdAuthDO;
import com.somle.amazon.model.AmazonAdClientDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AmazonAdClientRepository extends JpaRepository<AmazonAdClientDO, String> {
}
