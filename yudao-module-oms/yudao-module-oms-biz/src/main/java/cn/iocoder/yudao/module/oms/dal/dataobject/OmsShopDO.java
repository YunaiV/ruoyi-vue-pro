package cn.iocoder.yudao.module.oms.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

/**
 * OMS店铺表
 */
@TableName("oms_shop_maomao")
@KeySequence("oms_shop_maomao_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OmsShopDO extends TenantBaseDO {
    /**
     * 店铺id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 店铺名称
     */
    private String name;

    /**
     * 平台店铺名称
     */
    private String platformShopName;

    /**
     * 店铺编码
     */
    private String code;

    /**
     * 平台店铺编码
     */
    private String platformShopCode;


    /**
     * 平台名称
     */
    private String platformCode;

    /**
     * 店铺类型
     */
    private String type;
}