package com.somle.kingdee.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import lombok.Data;

@Entity
@Data
public class KingdeeToken {
    private String id;
    @Id
    private String appKey;
    private String appSecret;
    private String oldAppSecret;
    private int status;
    @Column(columnDefinition="LONGTEXT")
    private String accessToken;
    @Column(columnDefinition="LONGTEXT")
    private String appToken;
    private String expires;
    private String userNumber;
    private String serviceId;
    private String clientId;
    private String externalNumber;
    private String outerInstanceId;
    private String agreementCompanyName;
    private String agreementAuthProductType;
    private String agreementAdministratorName;
    private String agreementAdministratorAccountNumber;
    private String tenantId;
    private String accountId;
    private String uid;
    private int tid;
    private String createTime;
    private String openTime;
    private String secretUpdateTime;
    private String isBrand;
    private String brandSupplierId;
    private String groupName;
    private String accountName;
    private String domain;
    private String instanceId;
    private long instanceExpiresTime;
    private String isRetainData;
    private String trialOrderId;


    //custom
    private String appSignature;
}