package com.somle.erp.model.product;

import java.util.List;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


// @AllArgsConstructor
// @NoArgsConstructor
// class Sku implements Serializable {
//     protected String modelSku;
//     protected String styleCode;
//     protected String countryCode;
// }


@Slf4j
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
// @Inheritance(strategy = InheritanceType.JOINED)
public class ErpStyleSku {

    // @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    // private Long id;

    // @Id
    //@NotNull
    private String modelSku;
    // @Id
    //@NotNull
    private String styleCode;
    @Id
    private String styleSku;

    //@NotNull
    private String nameZh;
    //@NotNull
    private String nameEn;

    private List<String> imageUrlList;

    //研发属性
    //@NotNull
    private Float weight;
    //@NotNull
    private Float length;
    //@NotNull
    private Float width;
    //@NotNull
    private Float height;
    //@NotNull
    private Float packageWeight;
    //@NotNull
    private Float packageLength;
    //@NotNull
    private Float packageWidth;
    //@NotNull
    private Float packageHeight;
    //@NotNull
    private String materialZh;
    //@NotNull
    private String materialEn;
    //@NotNull
    private Long researchDepartmentId;

    //采购属性
    //@NotNull
    private Float purchasePrice;
    //@NotNull
    private String purchasePriceCurrencyCode;
    //@NotNull
    private String defaultSupplierCode;


    //销售属性
    //@NotNull
    private Integer saleStatus;
    //@NotNull
    private Long saleDepartmentId;


    //金蝶    
    //@NotNull
    private String barcode;


    private Boolean synced;

    // public String getStyleSku() {
    //     log.debug("concatenating style sku");
    //     if ((getModelSku() + getStyleCode()).equals(null)) {
    //         log.debug("It is null");
    //     } else {
    //         log.debug("It is not null");
    //     }
    // }

    public void setStyleSku() {
        styleSku = getModelSku() + getStyleCode();
    }



    // @PrePersist
    // public void prePersist() {
    //     setStyleSku();
    // }
}



