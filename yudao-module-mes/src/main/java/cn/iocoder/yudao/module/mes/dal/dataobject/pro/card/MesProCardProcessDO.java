package cn.iocoder.yudao.module.mes.dal.dataobject.pro.card;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.process.MesProProcessDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MES 流转卡工序记录 DO
 *
 * @author 芋道源码
 */
@TableName("mes_pro_card_process")
@KeySequence("mes_pro_card_process_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesProCardProcessDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 流转卡编号
     *
     * 关联 {@link MesProCardDO#getId()}
     */
    private Long cardId;
    /**
     * 序号
     */
    private Integer sort;
    /**
     * 工序编号
     *
     * 关联 {@link MesProProcessDO#getId()}
     */
    private Long processId;
    /**
     * 进入工序时间
     */
    private LocalDateTime inputTime;
    /**
     * 出工序时间
     */
    private LocalDateTime outputTime;
    /**
     * 投入数量
     */
    private BigDecimal inputQuantity;
    /**
     * 产出数量
     */
    private BigDecimal outputQuantity;
    /**
     * 不合格品数量
     */
    private BigDecimal unqualifiedQuantity;
    /**
     * 工位编号
     *
     * 关联 {@link MesMdWorkstationDO#getId()}
     */
    private Long workstationId;
    /**
     * 操作人编号
     *
     * 关联 AdminUserDO#getId()
     */
    private Long userId;
    /**
     * 过程检验单编号
     *
     * TODO @芋艿：关联 mes_qc_ipqc 表，等 IPQC 模块迁移后再对接
     */
    private Long ipqcId;
    /**
     * 备注
     */
    private String remark;

}
