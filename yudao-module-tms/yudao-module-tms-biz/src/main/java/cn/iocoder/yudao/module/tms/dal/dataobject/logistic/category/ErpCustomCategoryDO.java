package cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 海关分类 DO
 *
 * @author 王岽宇
 */
@TableName("erp_custom_category")
@KeySequence("erp_custom_category_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpCustomCategoryDO extends TenantBaseDO {

    /**
     * 编号
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 材质-字典
     */
    private Integer material;
    /**
     * 报关品名
     */
    private String declaredType;
    /**
     * 英文品名
     */
    private String declaredTypeEn;

}