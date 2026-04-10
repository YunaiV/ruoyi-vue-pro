package cn.iocoder.yudao.module.mes.dal.dataobject.md.item;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * MES 物料产品 DO
 *
 * @author 芋道源码
 */
@TableName("mes_md_item")
@KeySequence("mes_md_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesMdItemDO extends BaseDO {

    /**
     * 物料编号
     */
    @TableId
    private Long id;
    /**
     * 物料编码
     */
    private String code;
    /**
     * 物料名称
     */
    private String name;
    /**
     * 规格型号
     */
    private String specification;
    /**
     * 计量单位编号
     *
     * 关联 {@link MesMdUnitMeasureDO#getId()}
     */
    private Long unitMeasureId;
    /**
     * 物料分类编号
     *
     * 关联 {@link MesMdItemTypeDO#getId()}
     */
    private Long itemTypeId;
    /**
     * 状态
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;
    /**
     * 是否启用安全库存
     */
    private Boolean safeStockFlag;
    /**
     * 最低库存量
     */
    private BigDecimal minStock;
    /**
     * 最高库存量
     */
    private BigDecimal maxStock;
    /**
     * 是否高值物料
     */
    private Boolean highValue;
    /**
     * 是否启用批次管理
     */
    private Boolean batchFlag;
    /**
     * 备注
     */
    private String remark;

}
