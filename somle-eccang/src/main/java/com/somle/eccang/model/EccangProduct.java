package com.somle.eccang.model;

import cn.iocoder.yudao.framework.common.deserializer.CustomListDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EccangProduct {

    /** 操作类型：ADD新增；EDIT编辑（必填） */
    private String actionType;

    /** 产品SKU代码（必填） */
    private String productSku;

    /** 产品名称（中文）（必填） */
    private String productTitle;

    /** 产品名称（英文）（必填） */
    private String productTitleEn;

    /** 申报品名（中文）（必填） */
    private String pdOverseaTypeCn;

    /** 申报品名（英文）（必填） */
    private String pdOverseaTypeEn;

    /** 申报价值（必填） */
    private Float productDeclaredValue;

    /** 申报币种，例如USD（必填） */
    private String pdDeclareCurrencyCode;

    /** 默认采购单价 */
    private Float productPurchaseValue;

    /** 进口申报价值（币种固定为USD） */
    private Float productImportDeclareValue;

    /** 默认采购单价（不含税） */
    private Float productPurchaseValueNotTax;

    /** 币种代码 */
    private String currencyCode;

    /** 产品重量（单位：Kg） */
    private Float productWeight;

    /** 默认供应商代码 */
    private String defaultSupplierCode;

    /** 商品单价 */
    private Float productPrice;

    /** 采购参考价（需要传入defaultSupplierCode字段才有效） */
    private Float referenceUnitPrice;

    /** 采购参考链接 */
    @JsonDeserialize(using = CustomListDeserializer.class)
    private List<String> refUrl;

    /** 一级品类ID */
    private Integer productCategoryId1;

    /** 二级品类ID */
    private Integer productCategoryId2;

    /** 三级品类ID */
    private Integer productCategoryId3;

    /** 一级品类代码 */
    private String procutCategoryCode1;

    /** 二级品类代码 */
    private String procutCategoryCode2;

    /** 三级品类代码 */
    private String procutCategoryCode3;

    /** 一级品类名称 */
    private String procutCategoryName1;

    /** 二级品类名称 */
    private String procutCategoryName2;

    /** 三级品类名称 */
    private String procutCategoryName3;

    /** 一级品类英文名称 */
    private String procutCategoryNameEn1;

    /** 二级品类英文名称 */
    private String procutCategoryNameEn2;

    /** 三级品类英文名称 */
    private String procutCategoryNameEn3;

    /** 运营方式：1代运营；2自运营（默认值为2） */
    private Integer operationType;

    /** 产品状态：1可用；2开发中（默认值为1） */
    private Integer productStatus;

    /** 产品销售状态，可自定义 */
    private Integer saleStatus;

    /** 产品包装尺寸（长，单位CM） */
    private Float productLength;

    /** 产品包装尺寸（宽，单位CM） */
    private Float productWidth;

    /** 产品包装尺寸（高，单位CM） */
    private Float productHeight;

    /** 产品规格 */
    private String productSpecs;

    /** 产品净尺寸（长，单位CM） */
    private Float pdNetLength;

    /** 产品净尺寸（宽，单位CM） */
    private Float pdNetWidth;

    /** 产品净尺寸（高，单位CM） */
    private Float pdNetHeight;

    /** 产品净重（单位：KG） */
    private Float pdNetWeight;

    /** 产品包裹重量（单位：KG） */
    private Float productPackageWeight;

    /** 重量允许浮动值（单位：KG） */
    private Float allowFloatWeight;

    /** FOB税率，例如0.034 */
    private Float fboTaxRate;

    /** 开始设计时间，格式：YYYY-MM-DD HH:MM:SS */
    private String designerStartTime;

    /** 截止设计时间，格式：YYYY-MM-DD HH:MM:SS */
    private String designerEndTime;

    /** 设计师ID */
    private Integer designerId;

    /** 采购负责人ID */
    private Integer personOpraterId;

    /** 销售负责人ID */
    private Integer personSellerId;

    /** 开发负责人ID */
    private Integer personDevelopId;

    /** 产品颜色ID */
    private Integer productColorId;

    /** 产品尺寸ID */
    private Integer productSizeId;

    /** UPC代码 */
    private String upcCode;

    /** 产品材质 */
    private String productMaterial;

    /** 英文材质 */
    private String materialEn;

    /** FBA仓租尺寸类型：0无；1标准尺寸；2超标尺寸 */
    private Integer productFbaSizeType;

    /** 供应商交期（单位：天） */
    private Integer spEtaTime;

    /** 供应商最小采购量 */
    private Integer spMinQty;

    /** 供应商产品地址 */
    private List<String> spProductAddress;

    /** 产品等级ID */
    private Integer prlId;

    /** 父产品ID */
    private Integer parentProductId;

    /** 是否成品：1是；0否 */
    private Integer isEndProduct;

    /** 是否含电池：1是；0否 */
    private Integer containBattery;

    /** 是否仿制品：1是；0否 */
    private Integer isImitation;

    /** 是否需要质检：1是；0否 */
    private Integer isQc;

    /** 质检模板名称 */
    private String qcTemplateName;

    /** 是否组合产品：1是；0否 */
    private Integer productIsCombination;

    /** 贴标类型：1简单；2普通；3困难 */
    private String labelingType;

    /** 自定义属性列表 */
    private List<SelfProperty> selfPropertyList;

    /** 品牌代码 */
    private String brandCode;

    /** 品牌名称 */
    private String brandName;

    /** 是否有有效期：0无；1有 */
    private Integer isExpDate;

    /** 有效期天数 */
    private Integer expDate;

    /** 仓库条码列表 */
    private List<WarehouseBarcode> warehouseBarcodeList;

    /** 产品原产地（三字码，例如：CHN） */
    private String productOrigin;

    /** 毛利，例如：0.05 */
    private Float grossProfit;

    /** 是否赠品：1是；0否 */
    private Integer isGift;

    /** 税率，例如：0.05 */
    private Float taxRate;

    /** 产品图片地址列表 */
    private List<String> productImgUrlList;

    /** 产品描述 */
    private String desc;

    /** 用户组织ID */
    private Integer userOrganizationId;

    /** 默认发货仓库ID */
    private Integer defaultWarehouseId;

    /** EAN码 */
    private String eanCode;

    /** 自定义分类ID，逗号分隔 */
    private String pucId;

    /** 默认采购仓库ID */
    private Integer defaultBuyWarehouseId;

    /** 产品物流属性（逗号分隔，例如"2,3"） */
    private String logisticAttribute;

    /** 配件列表 */
    private List<Fitting> fitting;

    /** 包材列表 */
    private List<Package> packageList;

    /** 是否含特货：0否；1是 */
    private Integer containSpecialGoods;

    /** 是否含非液体化妆品：0否；1是 */
    private Integer containNonLiquidCosmetics;

    /** 产品周期性：1无；2季节性；3节假性 */
    private Integer periodic;

    /** 侵权等级ID */
    private Integer prtId;

    /** 销售员列表 */
    private List<String> seller;

    /** 产品单位 */
    private String unitCode;

    /** 中文用途 */
    private String use;

    /** 英文用途 */
    private String useEn;

    /** 产品海关属性 */
    private String pdProductCoustomsAttribute;

    /** 产品报关声明 */
    private String pdDeclarationStatement;

    /** 查询时是否返回箱规 */
    private Integer getProductBox;

    /** 产品箱规 */
    private List<Map<String, Object>> boxArr;

    /** 返回时的箱规信息 */
    private List<Map<String, Object>> productBox;

    @JsonIgnore
    public String getProductCategoryName() {
        return Stream.of(procutCategoryName3, procutCategoryName2, procutCategoryName1)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null); // 使用 orElse("默认值") 提供一个默认值
    }

}

/** 自定义属性类 */
class SelfProperty {
    private String name;
    private String value;
}

/** 仓库条码类 */
class WarehouseBarcode {
    private String warehouseCode;
    private String barcode;
}

/** 配件类 */
class Fitting {
    private String productSku;
    private String productTitle;
    private Integer fittingQty;
    private String fittingDesc;
}

/** 包材类 */
class Package {
    private String warehouseCode;
    private Integer packageQty;
    private String packageCode;
}
