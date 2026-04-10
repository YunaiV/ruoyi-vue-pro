package cn.iocoder.yudao.module.mes.dal.dataobject.wm.outsourceissue;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * MES 外协发料单行 DO
 *
 * @author 芋道源码
 */
@TableName("mes_wm_outsource_issue_line")
@KeySequence("mes_wm_outsource_issue_line_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmOutsourceIssueLineDO extends BaseDO {

    /**
     * 行ID
     */
    @TableId
    private Long id;
    /**
     * 发料单ID
     *
     * 关联 {@link MesWmOutsourceIssueDO#getId()}
     */
    private Long issueId;
    /**
     * 库存ID
     */
    private Long materialStockId;
    /**
     * 物料ID
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * 发料数量
     */
    private BigDecimal quantity;
    /**
     * 批次ID
     */
    private Long batchId;
    /**
     * 备注
     */
    private String remark;

}
