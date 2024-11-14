package com.somle.manomano.repository;

import com.somle.manomano.model.ManomanoShop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManomanoShopRepository extends JpaRepository<ManomanoShop, Long> {
}

