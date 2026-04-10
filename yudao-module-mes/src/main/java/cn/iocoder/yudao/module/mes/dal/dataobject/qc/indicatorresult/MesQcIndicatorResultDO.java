package cn.iocoder.yudao.module.mes.dal.dataobject.qc.indicatorresult;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.ipqc.MesQcIpqcDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.iqc.MesQcIqcDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.oqc.MesQcOqcDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.rqc.MesQcRqcDO;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MES 检验结果记录 DO
 *
 * @author 芋道源码
 */
@TableName("mes_qc_indicator_result")
@KeySequence("mes_qc_indicator_result_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesQcIndicatorResultDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 样品编号
     */
    private String code;
    /**
     * 关联质检单 ID（IQC/IPQC/OQC/RQC 的 id）
     *
     * 关联 IQC {@link MesQcIqcDO#getId()}
     * 关联 IPQC {@link MesQcIpqcDO#getId()}
     * 关联 OQC {@link MesQcOqcDO#getId()}
     * 关联 RQC {@link MesQcRqcDO#getId()}
     */
    private Long qcId;
    /**
     * 质检类型
     *
     * 枚举 {@link MesQcTypeEnum}
     */
    private Integer qcType;
    /**
     * 产品物料 ID
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * 物资 SN
     */
    private String sn;
    /**
     * 备注
     */
    private String remark;

}
