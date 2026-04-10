package cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnissue;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.rqc.MesQcRqcDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.batch.MesWmBatchDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.materialstock.MesWmMaterialStockDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * MES 生产退料单行 DO
 */
@TableName("mes_wm_return_issue_line")
@KeySequence("mes_wm_return_issue_line_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmReturnIssueLineDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 退料单 ID
     *
     * 关联 {@link MesWmReturnIssueDO#getId()}
     */
    private Long issueId;
    /**
     * 库存记录 ID
     *
     * 关联 {@link MesWmMaterialStockDO#getId()}
     */
    private Long materialStockId;
    /**
     * 物料 ID
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * 退料数量
     */
    private BigDecimal quantity;
    /**
     * 批次 ID
     *
     * 关联 {@link MesWmBatchDO#getId()}
     */
    private Long batchId;
    /**
     * 批次编码
     *
     * 关联 {@link MesWmBatchDO#getCode()}
     */
    private String batchCode;
    /**
     * 退货检验单 ID
     *
     * 关联 {@link MesQcRqcDO#getId()}
     */
    private Long rqcId;
    /**
     * 是否需要质检
     */
    private Boolean rqcCheckFlag;
    /**
     * 质量状态
     *
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.wm.MesWmQualityStatusEnum}
     */
    private Integer qualityStatus;
    /**
     * 备注
     */
    private String remark;

}
