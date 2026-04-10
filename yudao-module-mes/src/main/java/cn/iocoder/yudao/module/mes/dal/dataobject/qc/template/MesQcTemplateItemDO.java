package cn.iocoder.yudao.module.mes.dal.dataobject.qc.template;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * MES 质检方案-产品关联 DO
 *
 * @author 芋道源码
 */
@TableName("mes_qc_template_item")
@KeySequence("mes_qc_template_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesQcTemplateItemDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 质检方案编号
     *
     * 关联 {@link MesQcTemplateDO#getId()}
     */
    private Long templateId;
    /**
     * 产品物料编号
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * 最低检测数
     */
    private Integer quantityCheck;
    /**
     * 最大不合格数（0=不启用）
     */
    private Integer quantityUnqualified;
    /**
     * 最大致命缺陷率（%，0=不允许）
     */
    private BigDecimal criticalRate;
    /**
     * 最大严重缺陷率（%，0=不允许）
     */
    private BigDecimal majorRate;
    /**
     * 最大轻微缺陷率（%）
     */
    private BigDecimal minorRate;
    /**
     * 备注
     */
    private String remark;

}
