package cn.iocoder.yudao.module.erp.dal.dataobject.purchase.bo;

import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.erp.enums.status.SrmAuditStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ErpPurchaseOrderItemBO extends ErpPurchaseOrderItemDO {
    /**
     * 采购订单号，生成是由后端直接生成，默认是自动填充的，并且格式为：前缀+日期+6为顺序号（如：pre1-20241010-000001）
     */
    private String no;
    /**
     * 单据日期
     */
    private LocalDateTime noTime;

    /**
     * 审核状态
     * 枚举 {@link SrmAuditStatus}
     */
    private Integer auditStatus;
    /**
     * 供应商编号
     * 关联 {@link cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO#getId()}
     */
    private Long supplierId;
    /**
     * 结算账户编号
     * 关联 {@link cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpAccountDO#getId()}
     */
    private Long accountId;
    /**
     * 下单时间
     */
    private LocalDateTime orderTime;
    // ========== 合计 ==========
    /**
     * 合计数量-项目数量
     */
    private BigDecimal totalCount;
    /**
     * 最终合计价格，单位：元
     * <p>
     * totalPrice = totalProductPrice + totalTaxPrice - discountPrice
     */
    private BigDecimal totalPrice;
    /**
     * 合计产品价格，单位：元
     */
    private BigDecimal totalProductPrice;
    /**
     * 合计税额，单位：元
     */
    private BigDecimal totalTaxPrice;
    /**
     * 优惠率，百分比
     */
    private BigDecimal discountPercent;
    /**
     * 优惠金额，单位：元
     * <p>
     * discountPrice = (totalProductPrice + totalTaxPrice) * discountPercent
     */
    private BigDecimal discountPrice;
    /**
     * 定金金额，单位：元
     */
    private BigDecimal depositPrice;
    // ========== 采购文件信息 ==========
    /**
     * 附件地址
     */
    private String fileUrl;

    private LocalDateTime settlementDate;
    /**
     * 审核人id
     */
    private Long auditorId;
    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
    // ========== 部门和主体信息 ==========
    /**
     * 采购主体，进行采购的公司主体，关联财务模块-主体管理
     * <p>
     * 采购主体id
     */
    private Long purchaseEntityId;

    /**
     * 收货地址
     */
    private String address;
    /**
     * 付款条款
     */
    private String paymentTerms;
    // ========== 采购入库 ==========
    /**
     * 采购入库数量
     */
    private BigDecimal totalInCount;
    // ========== 采购退货（出库）） ==========
    /**
     * 采购退货数量
     */
    private BigDecimal totalReturnCount;
    // ========== 其他 ==========
    /**
     * 验货单，JSON 格式
     */
    private String inspectionJson;
    /**
     * 完工单，JSON 格式
     */
    private String completionJson;

    //审核意见
    private String reviewComment;
    //总验货通过数量
    private Integer totalInspectionPassCount;
}
