package com.somle.amazon.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmazonCountry {
    @Id
    private String code;
    private String marketplaceId;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="region_code")
    private AmazonRegion region;
}