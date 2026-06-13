package cn.iocoder.yudao.module.mes.dal.dataobject.wm.productproduce;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.feedback.MesProFeedbackDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 生产入库单行 DO
 *
 * @author 芋道源码
 */
@TableName("mes_wm_product_produce_line")
@KeySequence("mes_wm_product_produce_line_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmProductProduceLineDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 入库单 ID
     *
     * 关联 {@link MesWmProductProduceDO#getId()}
     */
    private Long produceId;
    /**
     * 报工记录 ID
     *
     * 关联 {@link MesProFeedbackDO#getId()}
     */
    private Long feedbackId;
    /**
     * 物料 ID
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * 入库数量
     */
    private BigDecimal quantity;
    /**
     * 批次 ID
     */
    private Long batchId;
    /**
     * 批次号
     */
    private String batchCode;
    /**
     * 过期日期
     */
    private LocalDateTime expireDate;
    /**
     * 生产批号
     */
    private String lotNumber;
    /**
     * 质量状态
     *
     * 字典 {@link DictTypeConstants#MES_WM_QUALITY_STATUS}
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.wm.MesWmQualityStatusEnum}
     */
    private Integer qualityStatus;
    /**
     * 备注
     */
    private String remark;

}
