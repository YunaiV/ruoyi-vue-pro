package com.somle.walmart.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "walmart_shipnode")
public class WalmartShipNode {
    private String clientId;
    @Id
    private String shipNode;
    private String code;
}
