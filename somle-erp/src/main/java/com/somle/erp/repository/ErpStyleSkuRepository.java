package com.somle.erp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.somle.erp.model.product.ErpStyleSku;

public interface ErpStyleSkuRepository extends JpaRepository<ErpStyleSku, Long>, JpaSpecificationExecutor<ErpStyleSku> {

    Optional<ErpStyleSku> findByStyleSku(String styleSku);
}

