package com.somle.erp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.somle.erp.model.product.ErpCountrySku;

public interface ErpCountrySkuRepository extends JpaRepository<ErpCountrySku, Long> {
}

