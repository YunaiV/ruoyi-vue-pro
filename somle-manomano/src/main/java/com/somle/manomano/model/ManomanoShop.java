package com.somle.manomano.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManomanoShop {
    @Id
    private Long id;
    @ManyToOne
    @JoinColumn(name = "token_id")
    private ManomanoToken token;
    private String contractId;
    private String countryCode;

}
