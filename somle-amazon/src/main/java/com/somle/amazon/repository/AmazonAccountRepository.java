package com.somle.amazon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.somle.amazon.model.AmazonAccount;


@Repository
public interface AmazonAccountRepository extends JpaRepository<AmazonAccount, Integer> {
}