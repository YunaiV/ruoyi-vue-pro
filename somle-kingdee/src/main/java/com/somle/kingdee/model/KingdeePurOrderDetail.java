package com.somle.kingdee.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author: Wqh
 * @date: 2024/12/25 9:31
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeePurOrderDetail {
    // 附件个数
    private int attachments;
    // 附件地址列表
    private List<Attachments> attachmentsUrl;
    // 审核时间
    private String auditTime;
    // 审核人ID
    private String auditorId;
    // 审核人名称
    private String auditorName;
    // 审核人编码
    private String auditorNumber;
    // 关闭状态
    private String billCloseState;
    // 单据日期
    private String billDate;
    // 单据编码
    private String billNo;
    // 单据状态
    private String billStatus;
    // 联系地址
    private String contactAddress;
    // 供应商发货地址市区ID
    private String contactCityId;
    // 供应商发货地址市区名称
    private String contactCityName;
    // 供应商发货地址市区编码
    private String contactCityNumber;
    // 供应商发货地址国家ID
    private String contactCountryId;
    // 供应商发货地址国家名称
    private String contactCountryName;
    // 供应商发货地址国家编码
    private String contactCountryNumber;
    // 供应商发货地址区县ID
    private String contactDistrictId;
    // 供应商发货地址区县名称
    private String contactDistrictName;
    // 供应商发货地址区县编码
    private String contactDistrictNumber;
    // 联系电话
    private String contactPhone;
    // 供应商发货地址省份ID
    private String contactProvinceId;
    // 供应商发货地址省份名称
    private String contactProvinceName;
    // 供应商发货地址省份编码
    private String contactProvinceNumber;
    // 创建时间
    private String createTime;
    // 创建人ID
    private String creatorId;
    // 创建人名称
    private String creatorName;
    // 创建人编码
    private String creatorNumber;
    // 币别ID
    private String currencyId;
    // 自定义字段使用指南
    private Map<String, String> customField;
    // 客户ID
    private String customerId;
    // 客户名称
    private String customerName;
    // 客户编码
    private String customerNumber;
    // 部门ID
    private String deptId;
    // 部门名称
    private String deptName;
    // 部门编码
    private String deptNumber;
    // 收货地址 - 详细地址
    private String dispatcherAddress;
    // 收货地址 - 市ID
    private String dispatcherCityId;
    // 收货地址市区名称
    private String dispatcherCityName;
    // 收货地址市区编码
    private String dispatcherCityNumber;
    // 收货地址国家ID
    private String dispatcherCountryId;
    // 收货地址国家名称
    private String dispatcherCountryName;
    // 收货地址国家编码
    private String dispatcherCountryNumber;
    // 收货地址 - 区县ID
    private String dispatcherDistrictId;
    // 收货地址区县名称
    private String dispatcherDistrictName;
    // 收货地址区县编码
    private String dispatcherDistrictNumber;
    // 收货地址人
    private String dispatcherLinkman;
    // 收货地址联系电话
    private String dispatcherPhone;
    // 收货地址 - 省ID
    private String dispatcherProvinceId;
    // 收货地址省份名称
    private String dispatcherProvinceName;
    // 收货地址省份编码
    private String dispatcherProvinceNumber;
    // 结算日期
    private String dueDate;
    // 业务员ID
    private String empId;
    // 业务员名称
    private String empName;
    // 业务员编码
    private String empNumber;
    // 汇率
    private String exchangeRate;
    // 单据ID
    private String id;
    // 入库状态
    private String ioStatus;
    // 商品分录列表
    private List<MaterialEntity> materialEntity;
    // 修改人ID
    private String modifierId;
    // 修改人名称
    private String modifierName;
    // 修改人编码
    private String modifierNumber;
    // 修改时间
    private String modifyTime;
    // 备注
    private String remark;
    // 发送状态
    private String sendStatus;
    // 结算期限ID
    private String settingTermId;
    // 结算期限名称
    private String settingTermName;
    // 结算期限编码
    private String settingTermNumber;
    // 供应商ID
    private String supplierId;
    // 供应商名称
    private String supplierName;
    // 供应商编码
    private String supplierNumber;
    // 业务类型
    private String transType;

    // 附件内部类
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class MaterialEntity {
        // 交货日期
        private String deliveryDate;
        // 实际不含税金额
        private double actNonTaxAmount;
        // 实际含税单价
        private double actTaxPrice;
        // 价税合计
        private double allAmount;
        // 金额
        private double amount;
        // 辅助换算率
        private double auxCoefficient;
        // 辅助属性1 ID
        private String auxId1;
        // 辅助属性2 ID
        private String auxId2;
        // 辅助属性3 ID
        private String auxId3;
        // 辅助属性1名称
        private String auxName1;
        // 辅助属性2名称
        private String auxName2;
        // 辅助属性3名称
        private String auxName3;
        // 辅助属性1编码
        private String auxNumber1;
        // 辅助属性2编码
        private String auxNumber2;
        // 辅助属性3编码
        private String auxNumber3;
        // 辅助属性ID
        private String auxPropId;
        // 辅助属性名称
        private String auxPropName;
        // 辅助属性编码
        private String auxPropNumber;
        // 辅助单位数量
        private double auxQty;
        // 辅助单位ID
        private String auxUnitId;
        // 辅助单位名称
        private String auxUnitName;
        // 辅助单位编码
        private String auxUnitNumber;
        // 条形码
        private String barcode;
        // 基本数量
        private double baseQty;
        // 基本单位ID
        private String baseUnitId;
        // 基本单位名称
        private String baseUnitName;
        // 基本单位编码
        private String baseUnitNumber;
        // 批次号
        private String batchNo;
        // 整单折扣分配额
        private String billDisDistribution;
        // 增值税税率
        private double cess;
        // 行关闭状态(0已关闭，1未关闭，2手动关闭)
        private String closeState;
        // 换算率
        private double coefficient;
        // 行备注
        private String comment;
        // 换算公式
        private String conversionRate;
        // 成本
        private double cost;
        // 本次核销金额
        private double curSettleAmount;
        // 自定义字段使用指南
        private Map<String, String> customEntityField;
        // 折扣额
        private double disAmount;
        // 折扣单价
        private double disPrice;
        // 折算率
        private double disRate;
        // 折扣
        private double discount;
        // 优惠分摊金额
        private double divideDiffAmount;
        // 行执行状态：A - 未执行 Z - 部分执行 C - 全部执行
        private String entryIosStatus;
        // 行入库状态：A - 未入库 Z - 部分入库 C - 全部入库
        private String entryRealioStatus;
        // 分录核销状态，未收款：A，部分收款：B，全部收款：C
        private String entrySettleStatus;
        // 销售费用分摊
        private double fee;
        // 商品分录ID
        private String id;
        // 已执行入库数量
        private double inQty;
        // 基本库存数量
        private double invBaseQty;
        // 库存数量
        private double invQty;
        // 是否赠品
        private boolean isFree;
        // 生产日期
        private String kfDate;
        // 保质期天数
        private int kfPeriod;
        // 保质期类型：1: 天，2: 月，3: 年
        private String kfType;
        // 商品助记码
        private String materialHelpCode;
        // 商品ID
        private String materialId;
        // 商品是否启用辅助属性
        private boolean materialIsAsstAttr;
        // 商品是否开启批次
        private boolean materialIsBatch;
        // 商品是否开启保质期
        private boolean materialIsKfPeriod;
        // 商品是否多单位
        private boolean materialIsMultiUnit;
        // 商品是否序列号
        private boolean materialIsSerial;
        // 商品规格
        private String materialModel;
        // 商品名称
        private String materialName;
        // 商品编码
        private String materialNumber;
        // 图片URL
        private String picture;
        // 折前金额
        private double preDisAmount;
        // 单价
        private double price;
        // 生产许可证号
        private String proLicense;
        // 产地
        private String proPlace;
        // 注册号
        private String proRegNo;
        // 数量
        private double qty;
        // 退货基本数量
        private double returnQty;
        // 退货数量
        private double returnQtyUnit;
        // 分录序号
        private int seq;
        // 序列号，格式：001: 备注1
        private String snList;
        // 序列号流转ID
        private String snListId;
        // 仓位ID
        private String spId;
        // 仓位名称
        private String spName;
        // 仓位编码
        private String spNumber;
        // 源单日期
        private String srcBillDate;
        // 源单号
        private String srcBillNo;
        // 源单类型ID
        private String srcBillTypeId;
        // 源单类型名称
        private String srcBillTypeName;
        // 源单类型编码
        private String srcBillTypeNumber;
        // 源单分录ID
        private String srcEntryId;
        // 源单ID
        private String srcInterId;
        // 源单ID
        private String srcOrderId;
        // 源单行号
        private int srcSeq;
        // 仓库ID
        private String stockId;
        // 仓库是否启用仓位管理
        private boolean stockIsAllowFreight;
        // 仓库名称
        private String stockName;
        // 仓库编码
        private String stockNumber;
        // 供应商商品名称(外部商品名称)
        private String suppMaterialName;
        // 供应商商品编码(外部商品编码)
        private String suppMaterialNumber;
        // 税额
        private double taxAmount;
        // 含税单价
        private double taxPrice;
        // 单位成本
        private double unitCost;
        // 单位ID
        private String unitId;
        // 单位名称
        private String unitName;
        // 单位编码
        private String unitNumber;
        // 有效日期
        private String validDate;
    }

    // 商品分录内部类
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Attachments {
        // 附件面板，暂时无用
        private String panel;
        // 附件地址
        private String url;
    }
}

