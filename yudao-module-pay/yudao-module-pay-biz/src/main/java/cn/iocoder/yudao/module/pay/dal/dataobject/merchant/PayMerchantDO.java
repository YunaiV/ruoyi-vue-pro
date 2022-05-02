package cn.iocoder.yudao.module.pay.dal.dataobject.merchant;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

/**
 * 支付商户信息 DO
 * 目前暂时没有特别的用途，主要为未来多商户提供基础。
 *
 * @author 芋道源码
 */
@TableName("pay_merchant")
@KeySequence("pay_merchant_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayMerchantDO extends BaseDO {

    /**
     * 商户编号，数据库自增
     */
    @TableId
    private Long id;
    /**
     * 商户号
     * 例如说，M233666999
     * 只有新增时插入，不允许修改
     */
    private String no;
    /**
     * 商户全称
     */
    private String name;
    /**
     * 商户简称
     */
    private String shortName;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
