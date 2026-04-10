package cn.iocoder.yudao.module.mes.dal.dataobject.qc.defectrecord;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.ipqc.MesQcIpqcDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.ipqc.MesQcIpqcLineDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.iqc.MesQcIqcDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.iqc.MesQcIqcLineDO;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcDefectLevelEnum;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MES 质检缺陷记录 DO
 *
 * 通用缺陷记录表，通过 {@link #qcType} 区分检验类型（IQC、IPQC、OQC、RQC），多模块复用
 *
 * @author 芋道源码
 */
@TableName("mes_qc_defect_record")
@KeySequence("mes_qc_defect_record_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesQcDefectRecordDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 检验类型
     *
     * 枚举 {@link MesQcTypeEnum}
     */
    private Integer qcType;
    /**
     * 检验单 ID
     *
     * 关联 {@link MesQcIqcDO#getId()}
     * 关联 {@link MesQcIpqcDO#getId()}
     */
    private Long qcId;
    /**
     * 检验行 ID
     *
     * 关联 {@link MesQcIqcLineDO#getId()}
     * 关联 {@link MesQcIpqcLineDO#getId()}
     */
    private Long lineId;
    /**
     * 缺陷描述
     */
    private String name;
    /**
     * 缺陷等级
     *
     * 枚举 {@link MesQcDefectLevelEnum}
     */
    private Integer level;
    /**
     * 缺陷数量
     */
    private Integer quantity;
    /**
     * 备注
     */
    private String remark;

}
