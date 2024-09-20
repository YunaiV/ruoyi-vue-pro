package com.somle.erp.model.product;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


// @AllArgsConstructor
// @NoArgsConstructor
// class Sku implements Serializable {
//     protected String modelSku;
//     protected String styleCode;
//     protected String countryCode;
// }

//@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErpPlatformSku {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String platformSku;

    private String platform;


    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_sku")
    List<ErpCountrySku> countrySkuList;

    private String saleDepartment;






}



