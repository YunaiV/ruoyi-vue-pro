package com.somle.matomo.repository;

import com.somle.matomo.model.MatomoToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MatomoTokenRepository extends JpaRepository<MatomoToken, String> {
}