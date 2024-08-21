package com.somle.amazon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.somle.amazon.model.AmazonShop;

@Repository
public interface AmazonShopRepository extends JpaRepository<AmazonShop, String> {
    AmazonShop findByCountryCode(String countryCode);
}

