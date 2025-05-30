package com.somle.cdiscount.model.resp;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CdiscountSellerRespVO {
    private Long sellerId;
    private String login;
    private LocalDateTime creationDate;
    private String shopName;
    private Boolean autoAcceptOrder;
}
