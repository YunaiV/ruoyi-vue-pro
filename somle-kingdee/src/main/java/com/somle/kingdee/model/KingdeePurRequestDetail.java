package com.somle.kingdee.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author: Wqh
 * @date: 2024/12/25 10:30
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeePurRequestDetail {
    // 附件个数
    private int attachments;
    // 附件地址列表
    private List<AttachmentsUrl> attachmentsUrl;
    // 审核时间
    private String auditTime;
    // 审核人ID
    private String auditorId;
    // 审核人名称
    private String auditorName;
    // 审核人编码
    private String auditorNumber;
    // 单据日期
    private String billDate;
    // 单据编码
    private String billNo;
    // 单据状态
    private String billStatus;
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
    // 自定义字段
    private Map<String, String> customField;
    // 部门ID
    private String deptId;
    // 部门名称
    private String deptName;
    // 部门编码
    private String deptNumber;
    // 收货详细地址（敏感数据解密）
    private String dispatcherAddress;
    // 收货市区ID
    private String dispatcherCityId;
    // 收货市区名称
    private String dispatcherCityName;
    // 收货市区编码
    private String dispatcherCityNumber;
    // 收货国家ID
    private String dispatcherCountryId;
    // 收货国家名称
    private String dispatcherCountryName;
    // 收货国家编码
    private String dispatcherCountryNumber;
    // 收货区县ID
    private String dispatcherDistrictId;
    // 收货区县名称
    private String dispatcherDistrictName;
    // 收货区县编码
    private String dispatcherDistrictNumber;
    // 收货人
    private String dispatcherLinkman;
    // 收货联系电话（敏感数据解密）
    private String dispatcherPhone;
    // 收货省份ID
    private String dispatcherProvinceId;
    // 收货省份名称
    private String dispatcherProvinceName;
    // 收货省份编码
    private String dispatcherProvinceNumber;
    // 业务员ID
    private String empId;
    // 业务员名称
    private String empName;
    // 业务员编码
    private String empNumber;
    // 单据ID
    private String id;
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
    // 采购状态（所有：“”，全部采购：“A”，部分采购：“P”，未采购：“N”）
    private String purchaseStatus;
    // 收货地址
    private String receviceDelivery;
    // 单据备注
    private String remark;
    // 建议供应商ID
    private String supplierId;
    // 建议供应商名称
    private String supplierName;
    // 建议供应商编码
    private String supplierNumber;
    // 业务类型，现购：1，赊购：2，直运采购：3，受托入库：4
    private String transType;


    // 附件地址内部类
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class AttachmentsUrl {
        // 附件类型，默认填写attachmentpanelap
        private String panel;
        // 服务器返回的附件地址
        private String url;
    }

    // 商品分录内部类
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class MaterialEntity {
        // 申请数量
        private double applyQty;
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
        // 换算率
        private double coefficient;
        // 行备注
        private String comment;
        // 换算公式
        private String conversionRate;
        // 入库成本
        private double cost;
        // 自定义字段
        private Map<String, String> customEntityField;
        // 辅助基本数量
        private double defFloatQty;
        // 折扣（小数）
        private double discount;
        // 分录ID
        private String id;
        // 已入库数量
        private String inQty;
        // 基本库存数量
        private double invBaseQty;
        // 库存数量
        private double invQty;
        // 入库状态（所有：“”，全部入库：“A”，部分入库：“P”，未入库：“N”）
        private String ioStatus;
        // 生产日期
        private String kfDate;
        // 保质期天数
        private int kfPeriod;
        // 保质期类型，1：天，2：月，3：年
        private String kfType;
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
        // 已订购数量
        private String orderedQty;
        // 图片URL
        private String picture;
        // 单价
        private double price;
        // 生产许可证号
        private String proLicense;
        // 产地
        private String proPlace;
        // 注册证号
        private String proRegNo;
        // 数量
        private double qty;
        // 分录序号
        private int seq;
        // 序列号 格式：001:备注1
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
        // 未入库数量
        private double unInQty;
        // 入库单位成本
        private double unitCost;
        // 单位ID
        private String unitId;
        // 单位名称
        private String unitName;
        // 单位编码
        private String unitNumber;
        // 未订购数量
        private String unorderedQty;
        // 有效日期
        private String validDate;
    }
}


