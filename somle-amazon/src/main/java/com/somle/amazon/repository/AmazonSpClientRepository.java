package com.somle.amazon.repository;

import com.somle.amazon.model.AmazonAdClientDO;
import com.somle.amazon.model.AmazonSpClientDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AmazonSpClientRepository extends JpaRepository<AmazonSpClientDO, String> {
}
