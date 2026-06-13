package cn.iocoder.yudao.module.mes.dal.dataobject.md.item;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 物料产品分类 DO
 *
 * @author 芋道源码
 */
@TableName("mes_md_item_type")
@KeySequence("mes_md_item_type_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesMdItemTypeDO extends BaseDO {

    public static final Long PARENT_ID_ROOT = 0L;

    /**
     * 分类编号
     */
    @TableId
    private Long id;
    /**
     * 分类编码
     */
    private String code;
    /**
     * 分类名称
     */
    private String name;
    /**
     * 父分类编号
     */
    private Long parentId;
    /**
     * 物料/产品标识
     *
     * 字典 {@link DictTypeConstants#MES_MD_ITEM_OR_PRODUCT}
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.md.MesMdItemTypeEnum}
     */
    private String itemOrProduct;
    /**
     * 显示排序
     */
    private Integer sort;
    /**
     * 状态
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
