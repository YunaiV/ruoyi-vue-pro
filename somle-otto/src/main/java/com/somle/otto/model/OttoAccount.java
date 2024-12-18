package com.somle.otto.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class OttoAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    public String clientId;
    public String clientSecret;
    //yyyy-mm-dd 到期日期：2025 年 6 月 16 日
    public String expirationDate;
}
