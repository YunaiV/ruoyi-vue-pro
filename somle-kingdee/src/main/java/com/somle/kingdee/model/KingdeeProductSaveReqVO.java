package com.somle.kingdee.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeeProductSaveReqVO {


    //自定义字段为引用辅助资料、引用基础资料时，提交单据保存接口，自定义字段名需要增加【_id】后缀，自定义字段值为：对应辅助资料或基础资料的id
    public void setCustomField(KingdeeCustomField customField, String value) {
        //5：基础资料，6：引用基础资料属性
        if (customField.getFieldType() == 5 || customField.getFieldType() == 6) {
            this.customField.put(customField.getNumber() + "_id", value);
        } else {
            this.customField.put(customField.getNumber(), value);
        }
    }

    private String baseUnitId; // 基本计量单位的ID，标识商品的基本单位
    private String checkType; // 商品类型，1为普通商品，2为套装，3为服务商品
    private String name; // 商品名称
    private String number; // 商品编码，唯一标识商品
    private String alarmDay; // 库存预警天数，指示库存接近最低水平时的提前告警时间
    private List<Auxentity> auxEntity; // 辅助属性列表，商品可能包含的各种附加信息
    private String auxUnitId; // 辅助单位ID，商品使用的辅助单位
    private String barcode; // 商品的条形码
    private List<BarcodeEntitySave> barcodeEntity; // 条形码实体对象，存储商品的条形码及相关信息
    private List<Bomentity> bomEntity; // 套装商品的BOM信息（物料清单），用于定义一个商品包含的其他商品
    private String brandId; // 品牌ID，标识商品所属的品牌
    private String brandNumber; // 品牌编码，标识品牌的唯一编号
    private Double coefficient1; // 换算率1，定义该商品和其他单位的换算比例
    private Double coefficient2; // 换算率2
    private Double coefficient3; // 换算率3
    private Integer computationRule; // 套装商品的分摊规则，1为按金额分摊，2为按成本分摊
    private String conversionUnit1Id; // 换算单位1的ID，标识用于换算的第一单位
    private String conversionUnit2Id; // 换算单位2的ID
    private String conversionUnit3Id; // 换算单位3的ID
    private String costMethod; // 成本计算方法，1为移动平均法，2为加权平均法，3为先进先出法
    private Map<String, String> customField = new HashMap<String, String>(); // 自定义字段，用于存储非标准的额外信息
    private String enable; // 商品是否启用，1为启用，0为禁用
    private String fetchCategoryId; // 税收分类编码，标识商品的税收类别
    private String fixUnitId1; // 辅助单位1的ID，用于多单位管理
    private String fixUnitId2; // 辅助单位2的ID
    private String fixUnitId3; // 辅助单位3的ID
    private String helpCode; // 商品的助记码，用于商品快速查询
    private String id; // 商品ID，主键字段，新增时不传，更新时必传
    private Boolean ignoreWarn; // 是否忽略警告信息，防止因重复或异常商品数据产生警告
    private List<String> images; // 商品的图片链接集合
    private String invMgrType; // 库存管理类型，0为统一库存，1为分仓库存
    private Boolean isAssembly; // 是否为可组装商品，指示该商品是否可以被组装成其他商品
    private Boolean isAsstAttr; // 是否启用辅助属性，决定商品是否可以有额外的辅助信息
    private Boolean isAuxCost; // 是否按辅助属性核算成本
    private Boolean isBatchCost; // 是否按批次核算成本
    private Boolean isBatch; // 是否启用批次管理
    private Boolean isFloat; // 是否启用浮动单位，允许单位在不同条件下变化
    private Boolean isInvAuxSet; // 是否启用库存辅助属性
    private Boolean isKfPeriod; // 是否启用保质期管理
    private Boolean isMultiUnit; // 是否启用多单位管理
    private Boolean isOutSource; // 是否允许委外加工
    private Boolean isPurchase; // 是否允许采购
    private Boolean isSale; // 是否允许销售
    private Boolean isSelfRestraint; // 是否为自制商品
    private Boolean isSerial; // 是否启用序列号管理
    private Boolean isSubpart; // 是否可作为子件使用
    private Boolean isWeight; // 是否启用称重功能
    private String kfPeriod; // 商品的保质期
    private String kfPeriodType; // 保质期的单位，1为天，2为月，3为年
    private String maxStockCheckQty; // 商品的最大库存量
    private String minStockCheckQty; // 商品的最小库存量
    private String model; // 商品的规格型号
    private String mulLabelNumber; // 商品标签编号，用于商品的分类或标记
    private String netWeight; // 商品的净重
    private String parentId; // 商品类别的父级ID
    private String parentNumber; // 商品类别的父级编码
    private List<PriceEntitySave> priceEntity; // 商品价格的实体对象，包含各种价格信息
    private String proLicense; // 商品的生产许可证号
    private String producingPace; // 商品的生产地
    private String purchaseId; // 采购员ID，指示该商品的采购员
    private String purchaseUnitId; // 采购单位ID，指示该商品的采购单位
    private String registrationNumber; // 商品的注册证号
    private String remark; // 商品的备注信息
    private String saleUnitId; // 销售单位ID
    private String secStockCheckQty; // 安全库存量
    private String spaceId; // 仓位ID，标识商品存放的仓库位置
    private String stockId; // 仓库ID，标识商品所在的仓库
    private List<Store> storeEntity; // 商品的库存管理实体
    private String storeUnitId; // 商品的库存单位ID
    private String taxRate; // 商品的税率
    private String venderId; // 供应商ID，标识商品的供应商
    private String volume; // 商品的体积
    private String volumeUnitId; // 体积单位ID
    private String weightUnitId; // 重量单位ID


    private String high;
    private String length;
    private String grossWeight;// 毛重
    private String wide;

    // private Float high;
    // private Float length;
    // private Float grossWeight;
    // private Float wide;

    // custom
    private Long saleDepartmentId; //销售部门 ID
    private String declaredTypeZh;
    private String declaredTypeEn;//报关品名英文
}

@Data
class Auxentity {
    private String id; // 辅助属性ID，唯一标识辅助属性
    private String auxCombinationName; // 辅助属性组合名称，多个属性的组合名称
    private Boolean auxEnable; // 是否启用辅助属性，true表示启用，false表示禁用
    private String auxId1; // 辅助属性1
    private String auxId2; // 辅助属性2
    private String auxId3; // 辅助属性3
    private String auxId4; // 辅助属性4
    private String auxId5; // 辅助属性5
    private String auxTypeId1; // 辅助属性类型1
    private String auxTypeId2; // 辅助属性类型2
    private String auxTypeId3; // 辅助属性类型3
    private String auxTypeId4; // 辅助属性类型4
    private String auxTypeId5; // 辅助属性类型5
    private String concatTypeId; // 多个辅助属性ID的连接串，用英文逗号分隔
    private String pictureUrl; // 辅助属性主图的URL
    private String skuId; // 商品的SKU ID
    private String skuNumber; // 商品的SKU编码

    // Constructors, getters, and setters can be added here
}

@Data
class BarcodeEntitySave {
    private String id; // 条形码实体ID，唯一标识条形码
    private String barcode; // 商品的条形码
    private String barcodeRemark; // 条形码备注
    private String barcodeUnitId; // 条形码对应的单位ID

    // Constructors, getters, and setters can be added here
}

@Data
class Bomentity {
    private String materialId; // 物料ID，标识套装商品中的子商品
    private String qty; // 数量，表示该物料在套装中的数量
    private String unitId; // 物料单位ID
    private String auxId; // 辅助属性ID
    private String auxId1; // 辅助属性1的ID
    private String auxId2; // 辅助属性2的ID
    private String auxId3; // 辅助属性3的ID
    private String auxId4; // 辅助属性4的ID
    private String auxId5; // 辅助属性5的ID
    private String entryCostPrice; // 物料的参考成本价格
    private String id; // 套装商品BOM记录的ID
    private Boolean isFree; // 是否为赠品，true表示赠品，false表示不是赠品
    private Boolean isReplace; // 是否可替换该物料，true表示可替换，false表示不可替换
    private String materialReplace; // 替换物料的商品编码，多个编码用英文逗号分隔
    private String price; // 物料单价

    // Constructors, getters, and setters can be added here
}

@Data
class PriceEntitySave {
    private String id; // 价格实体ID，唯一标识价格记录
    private String priceBarcode; // 价格条形码
    private String priceCostPrice; // 参考成本价格
    private String priceDistributionPrice; // 配送价
    private String priceMaxPurchasePrice; // 最高采购价
    private String priceMinPurTaxPrice; // 最低含税采购价
    private String priceMinSalesPrice; // 最低销售价
    private String priceNearPurPrice; // 最近采购单价
    private String priceNearPurTaxPrice; // 最近采购含税单价
    private String priceNearPurUnitCost; // 最近采购入库成本
    private String priceNearSalPrice; // 最近销售单价
    private String priceNearSalTaxPrice; // 最近销售含税单价
    private String priceNearSupplier; // 最近成交供应商
    private String priceOutsourcePrice; // 委外价格
    private String pricePurchasePrice; // 采购价格
    private String priceRetailPrice; // 零售价
    private String priceSalePrice1; // 价格等级1
    private String priceSalePrice10; // 价格等级10
    private String priceSalePrice2; // 价格等级2
    private String priceSalePrice3; // 价格等级3
    private String priceSalePrice4; // 价格等级4
    private String priceSalePrice5; // 价格等级5
    private String priceSalePrice6; // 价格等级6
    private String priceSalePrice7; // 价格等级7
    private String priceSalePrice8; // 价格等级8
    private String priceSalePrice9; // 价格等级9
    private String priceTradePrice; // 批发价格
    private String priceUnitId; // 价格单位ID
    private String priceUnitName; // 价格单位名称
    private String priceUnitNumber; // 价格单位编码

    // Constructors, getters, and setters can be added here
}

@Data
class Store {
    private String id; // 库存实体ID，唯一标识库存记录
    private String skuId; // 商品的SKU ID
    private String spaceId; // 仓位ID，标识商品所在仓位
    private String storeId; // 仓库ID，标识商品所在仓库
    private String maxQty; // 最大库存数量
    private String minQty; // 最小库存数量
    private String secQty; // 安全库存数量

    // Constructors, getters, and setters can be added here
}
