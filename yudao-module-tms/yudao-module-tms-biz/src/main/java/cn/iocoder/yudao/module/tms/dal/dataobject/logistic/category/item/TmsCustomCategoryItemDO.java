package cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.item;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * 海关分类子表 DO
 *
 * @author 王岽宇
 */
@TableName("erp_custom_category_item")
@KeySequence("erp_custom_category_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsCustomCategoryItemDO extends TenantBaseDO {

    /**
     * 编号
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 分类表id
     */
    private Long customCategoryId;
    /**
     * 国家-字典
     */
    private Integer countryCode;
    /**
     * HS编码
     */
    private String hscode;
    /**
     * 税率
     */
    private BigDecimal taxRate;

}