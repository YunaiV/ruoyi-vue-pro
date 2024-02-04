package cn.iocoder.yudao.module.erp.dal.dataobject.sale;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.LongListTypeHandler;
import cn.iocoder.yudao.module.erp.enums.sale.ErpSaleOrderStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ERP 销售订单 DO
 *
 * @author 芋道源码
 */
@TableName(value = "erp_sale_order", autoResultMap = true)
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
     * 销售单编号
     */
    private String no;
    /**
     * 销售状态
     *
     * 枚举 {@link ErpSaleOrderStatusEnum}
     */
    private Integer status;
    /**
     * 客户编号
     *
     * TODO 芋艿：关联
     */
    private Long customerId;
    /**
     * 结算账户编号
     *
     * TODO 芋艿：关联
     */
    private Long accountId;
    /**
     * 销售员编号数组
     *
     * TODO 芋艿：关联
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> salePersonIds;
    /**
     * 下单时间
     */
    private LocalDateTime orderTime;

    /**
     * 合计价格，单位：元
     */
    private BigDecimal totalPrice;
    /**
     * 优惠率，百分比
     */
    private BigDecimal discountPercent;
    /**
     * 优惠金额，单位：元
     */
    private BigDecimal discountPrice;
    /**
     * 支付金额，单位：元
     */
    private BigDecimal payPrice;
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
    private String description;

}