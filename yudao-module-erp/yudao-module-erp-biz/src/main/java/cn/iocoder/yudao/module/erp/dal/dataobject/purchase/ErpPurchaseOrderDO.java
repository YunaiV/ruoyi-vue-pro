package cn.iocoder.yudao.module.erp.dal.dataobject.purchase;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpAccountDO;
import cn.iocoder.yudao.module.erp.enums.status.ErpAuditStatus;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP 采购订单 DO
 * <p>
 * 包含整个采购订单的基本信息，例如订单编号、总金额、供应商信息等。
 */
@TableName(value = "erp_purchase_order")
@KeySequence("erp_purchase_order_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpPurchaseOrderDO extends TenantBaseDO {
    // ========== 基本信息 ==========
    @TableId
    private Long id;
    /**
     * 采购订单号，生成是由后端直接生成，默认是自动填充的，并且格式为：前缀+日期+6为顺序号（如：pre1-20241010-000001）
     */
    private String no;
    /**
     * 单据日期
     */
    private LocalDateTime noTime;
    /**
     * 采购状态
     * 枚举 {@link ErpAuditStatus}
     */
    private Integer status;
    /**
     * 供应商编号
     * 关联 {@link ErpSupplierDO#getId()}
     */
    private Long supplierId;
    /**
     * 结算账户编号
     * 关联 {@link ErpAccountDO#getId()}
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
    /**
     * 备注
     */
    private String remark;
    // ========== 时间相关字段 ==========
//    /**
//     * 单据日期，默认为当前日期，也可以自行选择
//     */
//    private LocalDateTime documentDate;
    /**
     * 结算日期，默认为当前日期
     */
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
     * 部门，由系统中进行选择
     * <p></>
     * 部门id
     */
    private Long departmentId;
    /**
     * 采购主体，进行采购的公司主体，关联财务模块-主体管理
     * <p></>
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

    /**
     * 关闭状态
     */
    private Integer offStatus;
    /**
     * 采购状态
     * {@link ErpAuditStatus}
     */
    private Integer orderStatus;
    //执行状态 Integer
    /**
     * 执行状态
     * {@link }
     */
    private Integer executeStatus;
    //入库状态
    private Integer inStatus;
    //付款状态
    private Integer payStatus;
    //审核状态
    private Integer auditStatus;
}
