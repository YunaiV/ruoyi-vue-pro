package cn.iocoder.yudao.module.crm.dal.dataobject.product;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.crm.enums.product.CrmProductStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

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
public class CrmProductDO extends BaseDO {

    /**
     * 主键 id
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
     * 关联 {@link CrmProductStatusEnum}
     */
    private Integer status;
    /**
     * 产品分类 ID
     * 关联 {@link CrmProductCategoryDO#id}
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
