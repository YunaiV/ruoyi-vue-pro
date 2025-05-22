package cn.iocoder.yudao.module.erp.dal.dataobject.sale;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpAccountDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP 销售订单 DO
 *
 * @author 芋道源码
 */
@TableName(value = "erp_sale_order")
@KeySequence("erp_sale_order_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpSaleOrderDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 销售订单号
     */
    private String no;
    /**
     * 销售状态
     *
     * 枚举 {@link cn.iocoder.yudao.module.erp.enums.ErpAuditStatus}
     */
    private Integer status;
    /**
     * 客户编号
     *
     * 关联 {@link ErpCustomerDO#getId()}
     */
    private Long customerId;
    /**
     * 结算账户编号
     *
     * 关联 {@link ErpAccountDO#getId()}
     */
    private Long accountId;
    /**
     * 销售员编号
     *
     * 关联 AdminUserDO 的 id 字段
     */
    private Long saleUserId;
    /**
     * 下单时间
     */
    private LocalDateTime orderTime;

    /**
     * 合计数量
     */
    private BigDecimal totalCount;
    /**
     * 最终合计价格，单位：元
     *
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
     *
     * discountPrice = (totalProductPrice + totalTaxPrice) * discountPercent
     */
    private BigDecimal discountPrice;
    /**
     * 定金金额，单位：元
     */
    private BigDecimal depositPrice;

    /**
     * 附件地址
     */
    private String fileUrl;
    /**
     * 备注
     */
    private String remark;

    // ========== 销售出库 ==========
    /**
     * 销售出库数量
     */
    private BigDecimal outCount;

    // ========== 销售退货（入库）） ==========
    /**
     * 销售退货数量
     */
    private BigDecimal returnCount;

}