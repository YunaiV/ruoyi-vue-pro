package cn.iocoder.yudao.module.oms.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OMS订单 DO
 *
 * @author 谷毛毛
 */
@TableName("oms_order")
@KeySequence("oms_order_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OmsOrderDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 所属平台
     */
    private String platformCode;
    /**
     * 订单号
     */
    private String no;
    /**
     * 外部来源号，即平台订单号
     */
    private String sourceNo;
    /**
     * 店铺id
     */
    private String shopId;
    /**
     * 运费
     */
    private BigDecimal shippingCost;
    /**
     * 总金额
     */
    private BigDecimal totalPrice;
    /**
     * 买家姓名
     */
    private String buyerName;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 最迟送达时间
     */
    private LocalDateTime deliveryLatestTime;
    /**
     * 订单创建时间
     */
    private LocalDateTime orderCreateTime;
    /**
     * 付款时间
     */
    private LocalDateTime paymentTime;
    /**
     * 电话
     */
    private String telephone;
    /**
     * 公司名
     */
    private String companyName;
    /**
     * 收件人国家
     */
    private String buyerCountryCode;
    /**
     * 收件人省【或为州】
     */
    private String state;
    /**
     * 城市
     */
    private String city;
    /**
     * 区/县
     */
    private String district;
    /**
     * 外部来源原地址，用作备份
     */
    private String sourceAddress;
    /**
     * 地址
     */
    private String address;
    /**
     * 门牌号
     */
    private String houseNumber;
    /**
     * 邮编
     */
    private String postalCode;

}