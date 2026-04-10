package cn.iocoder.yudao.module.mes.dal.dataobject.wm.arrivalnotice;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.iqc.MesQcIqcDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * MES 到货通知单行 DO
 */
@TableName("mes_wm_arrival_notice_line")
@KeySequence("mes_wm_arrival_notice_line_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmArrivalNoticeLineDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 到货通知单编号
     *
     * 关联 {@link MesWmArrivalNoticeDO#getId()}
     */
    private Long noticeId;
    /**
     * 物料编号
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * 到货数量
     */
    private BigDecimal arrivalQuantity;
    /**
     * 合格数量
     */
    private BigDecimal qualifiedQuantity;
    /**
     * 是否需要来料检验
     */
    private Boolean iqcCheckFlag;
    /**
     * 来料检验单编号
     *
     * 关联 {@link MesQcIqcDO#getId()}
     */
    private Long iqcId;
    /**
     * 备注
     */
    private String remark;

}
