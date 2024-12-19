package com.somle.otto.repository;

import com.somle.otto.model.pojo.OttoAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OttoAuthTokenDao extends JpaRepository<OttoAuthToken, Long> {
}
