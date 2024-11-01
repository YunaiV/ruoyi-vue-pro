package com.somle.shopify.repository;

import com.somle.shopify.model.ShopifyToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopifyTokenRepository extends JpaRepository<ShopifyToken, String> {
}

