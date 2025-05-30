package com.somle.tiktok.repository;


import com.somle.tiktok.model.TikTokAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TikTokAccountRepository extends JpaRepository<TikTokAccount, Long> {
}

