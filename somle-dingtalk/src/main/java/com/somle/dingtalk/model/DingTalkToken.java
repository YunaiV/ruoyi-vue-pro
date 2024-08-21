package com.somle.dingtalk.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import lombok.Data;

@Entity
@Data
public class DingTalkToken {
    @Id
    private String appKey;
    private String appSecret;
    private String accessToken;
}