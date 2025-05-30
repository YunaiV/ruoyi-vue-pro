package com.somle.walmart.repository;

import com.somle.walmart.model.WalmartShipNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalmartShipNodeRepository extends JpaRepository<WalmartShipNode, String> {
}
