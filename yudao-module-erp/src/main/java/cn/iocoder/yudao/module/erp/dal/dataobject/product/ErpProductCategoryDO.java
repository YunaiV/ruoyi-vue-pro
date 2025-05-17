package cn.iocoder.yudao.module.erp.dal.dataobject.product;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * ERP 产品分类 DO
 *
 * @author 芋道源码
 */
@TableName("erp_product_category")
@KeySequence("erp_product_category_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProductCategoryDO extends BaseDO {

    public static final Long PARENT_ID_ROOT = 0L;

    /**
     * 分类编号
     */
    @TableId
    private Long id;
    /**
     * 父分类编号
     */
    private Long parentId;
    /**
     * 分类名称
     */
    private String name;
    /**
     * 分类编码
     */
    private String code;
    /**
     * 分类排序
     */
    private Integer sort;
    /**
     * 开启状态
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;

}