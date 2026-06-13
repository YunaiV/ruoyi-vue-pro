package cn.iocoder.yudao.module.wms.dal.dataobject.md.item;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * WMS 商品分类 DO
 *
 * @author 芋道源码
 */
@TableName("wms_item_category")
@KeySequence("wms_item_category_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsItemCategoryDO extends BaseDO {

    /**
     * 父级编号 - 根节点
     */
    public static final Long PARENT_ID_ROOT = 0L;

    /**
     * 主键编号
     */
    @TableId
    private Long id;
    /**
     * 父级编号
     *
     * 关联 {@link #id}
     */
    private Long parentId;
    /**
     * 分类编号
     */
    private String code;
    /**
     * 分类名称
     */
    private String name;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
