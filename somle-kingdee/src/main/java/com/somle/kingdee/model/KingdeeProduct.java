package com.somle.kingdee.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.PropertyNamingStrategy;

import lombok.Data;

@Data
@JSONType(naming = PropertyNamingStrategy.SnakeCase)
public class KingdeeProduct {


    public void setCustomField(KingdeeCustomField customField, String value) {
        this.customField.put(customField.getNumber() + "_id", value);
    }

    private String baseUnitId;
    private String checkType;
    private String name;
    private String number;
    private String alarmDay;
    private List<Auxentity> auxEntity;
    private String auxUnitId;
    private String barcode;
    private List<BarcodeEntitySave> barcodeEntity;
    private List<Bomentity> bomEntity;
    private String brandId;
    private String brandNumber;
    private Double coefficient1;
    private Double coefficient2;
    private Double coefficient3;
    private Integer computationRule;
    private String conversionUnit1Id;
    private String conversionUnit2Id;
    private String conversionUnit3Id;
    private String costMethod;
    private Map<String, String> customField  = new HashMap<String, String>();
    private String enable;
    private String fetchCategoryId;
    private String fixUnitId1;
    private String fixUnitId2;
    private String fixUnitId3;
    private String helpCode;
    private String id;
    private Boolean ignoreWarn;
    private List<String> images;
    private String invMgrType;
    private Boolean isAssembly;
    private Boolean isAsstAttr;
    private Boolean isAuxCost;
    private Boolean isBatchCost;
    private Boolean isBatch;
    private Boolean isFloat;
    private Boolean isInvAuxSet;
    private Boolean isKfPeriod;
    private Boolean isMultiUnit;
    private Boolean isOutSource;
    private Boolean isPurchase;
    private Boolean isSale;
    private Boolean isSelfRestraint;
    private Boolean isSerial;
    private Boolean isSubpart;
    private Boolean isWeight;
    private String kfPeriod;
    private String kfPeriodType;
    private String maxInventoryQty;
    private String minInventoryQty;
    private String model;
    private String mulLabelNumber;
    private String netWeight;
    private String parentId;
    private String parentNumber;
    private List<PriceEntitySave> priceEntity;
    private String proLicense;
    private String producingPace;
    private String purchaseId;
    private String purchaseUnitId;
    private String registrationNumber;
    private String remark;
    private String saleUnitId;
    private String secInventoryQty;
    private String spaceId;
    private String stockId;
    private List<Store> storeEntity;
    private String storeUnitId;
    private String taxRate;
    private String venderId;
    private String volume;
    private String volumeUnitId;
    private String weightUnitId;

    private String high;
    private String length;
    private String grossWeight;
    private String wide;

    // private Float high;
    // private Float length;
    // private Float grossWeight;
    // private Float wide;

    // custom
    private Long saleDepartmentId;
    private String declaredTypeZh;
}

@Data
class Auxentity {
    private String id;
    private String auxCombinationName;
    private Boolean auxEnable;
    private String auxId1;
    private String auxId2;
    private String auxId3;
    private String auxId4;
    private String auxId5;
    private String auxTypeId1;
    private String auxTypeId2;
    private String auxTypeId3;
    private String auxTypeId4;
    private String auxTypeId5;
    private String concatTypeId;
    private String pictureUrl;
    private String skuId;
    private String skuNumber;

    // Constructors, getters, and setters can be added here
}

@Data
class BarcodeEntitySave {
    private String id;
    private String barcode;
    private String barcodeRemark;
    private String barcodeUnitId;

    // Constructors, getters, and setters can be added here
}

@Data
class Bomentity {
    private String materialId;
    private String qty;
    private String unitId;
    private String auxId;
    private String auxId1;
    private String auxId2;
    private String auxId3;
    private String auxId4;
    private String auxId5;
    private String entryCostPrice;
    private String id;
    private Boolean isFree;
    private Boolean isReplace;
    private String materialReplace;
    private String price;

    // Constructors, getters, and setters can be added here
}

@Data
class PriceEntitySave {
    private String id;
    private String priceBarcode;
    private String priceCostPrice;
    private String priceDistributionPrice;
    private String priceMaxPurchasePrice;
    private String priceMinPurTaxPrice;
    private String priceMinSalesPrice;
    private String priceNearPurPrice;
    private String priceNearPurTaxPrice;
    private String priceNearPurUnitCost;
    private String priceNearSalPrice;
    private String priceNearSalTaxPrice;
    private String priceNearSupplier;
    private String priceOutsourcePrice;
    private String pricePurchasePrice;
    private String priceRetailPrice;
    private String priceSalePrice1;
    private String priceSalePrice10;
    private String priceSalePrice2;
    private String priceSalePrice3;
    private String priceSalePrice4;
    private String priceSalePrice5;
    private String priceSalePrice6;
    private String priceSalePrice7;
    private String priceSalePrice8;
    private String priceSalePrice9;
    private String priceTradePrice;
    private String priceUnitId;
    private String priceUnitName;
    private String priceUnitNumber;

    // Constructors, getters, and setters can be added here
}

@Data
class Store {
    private String id;
    private String skuId;
    private String spaceId;
    private String storeId;
    private String maxQty;
    private String minQty;
    private String secQty;

    // Constructors, getters, and setters can be added here
}
