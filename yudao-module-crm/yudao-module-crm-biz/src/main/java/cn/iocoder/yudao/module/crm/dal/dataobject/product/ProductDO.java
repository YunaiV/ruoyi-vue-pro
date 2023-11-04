package cn.iocoder.yudao.module.crm.dal.dataobject.product;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 产品 DO
 *
 * @author ZanGe丶
 */
@TableName("crm_product")
@KeySequence("crm_product_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDO extends BaseDO {

    /**
     * 主键id
     */
    @TableId
    private Long id;
    /**
     * 产品名称
     */
    private String name;
    /**
     * 产品编码
     */
    private String no;
    /**
     * 单位
     */
    private String unit;
    /**
     * 价格
     */
    private Long price;
    /**
     * 状态
     *
     * 枚举 {@link TODO crm_product_status 对应的类}
     */
    private Integer status;
    /**
     * 产品分类ID
     */
    private Long categoryId;
    /**
     * 产品描述
     */
    private String description;
    /**
     * 负责人的用户编号
     */
    private Long ownerUserId;

}
