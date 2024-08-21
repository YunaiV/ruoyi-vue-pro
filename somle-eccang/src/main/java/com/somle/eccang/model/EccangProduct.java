package com.somle.eccang.model;

import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.annotation.JSONType;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Stream;



@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EccangProduct {
    private String actionType;
    private String productSku;
    private String productTitle;
    private String productTitleEn;
    private String pdOverseaTypeCn;
    private String pdOverseaTypeEn;
    private Float productDeclaredValue;
    private String pdDeclareCurrencyCode;
    private Float productPurchaseValue;
    private Float productImportDeclareValue;
    private Float productPurchaseValueNotTax;
    private String currencyCode;
    private Float productWeight;
    private String defaultSupplierCode;
    private Float productPrice;
    private Float referenceUnitPrice;
    private List<String> refUrl;

    private Integer productCategoryId1;
    private Integer productCategoryId2;
    private Integer productCategoryId3;

    private String procutCategoryCode1;
    private String procutCategoryCode2;
    private String procutCategoryCode3;

    private String procutCategoryName1;
    private String procutCategoryName2;
    private String procutCategoryName3;

    private String procutCategoryNameEn1;
    private String procutCategoryNameEn2;
    private String procutCategoryNameEn3;

    private Integer operationType;
    private Integer productStatus;
    private Integer saleStatus;
    private String hsCode;
    private Float productLength;
    private Float productWidth;
    private Float productHeight;
    private String productSpecs;
    private Float pdNetLength;
    private Float pdNetWidth;
    private Float pdNetHeight;
    private Float pdNetWeight;
    private Float productPackageWeight;
    private Float allowFloatWeight;
    private Float fboTaxRate;
    private String designerStartTime;
    private String designerEndTime;
    private Integer designerId;
    private Integer personOpraterId;
    private Integer personSellerId;
    private Integer personDevelopId;
    private Integer productColorId;
    private Integer productSizeId;
    private String upcCode;
    private String productMaterial;
    private String materialEn;
    private Integer productFbaSizeType;
    private Integer spEtaTime;
    private Integer spMinQty;
    private List<String> spProductAddress;
    private Integer prlId;
    private Integer parentProductId;
    private Integer isEndProduct;
    private Integer containBattery;
    private Integer isImitation;
    private Integer isQc;
    private String qcTemplateName;
    private Integer productIsCombination;
    private String labelingType;
    private List<SelfProperty> selfPropertyList;
    private String brandCode;
    private String brandName;
    private Integer isExpDate;
    private Integer expDate;
    private List<WarehouseBarcode> warehouseBarcodeList;
    private String productOrigin;
    private Float grossProfit;
    private Integer isGift;
    private Float taxRate;
    private List<String> productImgUrlList;
    private String desc;
    private Integer userOrganizationId;
    private Integer defaultWarehouseId;
    private String eanCode;
    private String pucId;
    private Integer defaultBuyWarehouseId;
    private String logisticAttribute;
    private List<Fitting> fitting;
    private List<Package> packageList;
    private Integer containSpecialGoods;
    private Integer containNonLiquidCosmetics;
    private Integer periodic;
    private Integer prtId;
    private List<String> seller;
    private String unitCode;
    private String use;
    private String useEn;
    private String pdProductCoustomsAttribute;
    private String pdDeclarationStatement;

    public String getProductCategoryName() {
        return Stream.of(procutCategoryName3, procutCategoryName2, procutCategoryName1)
            .filter(o -> o != null)
            .findFirst().get();
    }
}

class SelfProperty {
    private String name;
    private String value;

    // Getters and setters
}

class WarehouseBarcode {
    private String warehouseCode;
    private String barcode;

    // Getters and setters
}

class Fitting {
    private String productSku;
    private String productTitle;
    private Integer fittingQty;
    private String fittingDesc;

    // Getters and setters
}

class Package {
    private String warehouseCode;
    private Integer packageQty;
    private String packageCode;

    // Getters and setters
}
