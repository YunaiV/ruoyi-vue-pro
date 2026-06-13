package cn.iocoder.yudao.module.mes.dal.dataobject.md.item;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * MES 产品 BOM DO
 *
 * @author 芋道源码
 */
@TableName("mes_md_product_bom")
@KeySequence("mes_md_product_bom_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesMdProductBomDO extends BaseDO {

    /**
     * BOM编号
     */
    @TableId
    private Long id;
    /**
     * 物料产品编号
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * BOM物料编号
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long bomItemId;
    /**
     * 物料使用比例
     */
    private BigDecimal quantity;
    /**
     * 是否启用
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
