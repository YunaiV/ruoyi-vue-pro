package cn.iocoder.yudao.module.mes.dal.dataobject.qc.indicator;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MES 质检指标 DO
 *
 * @author 芋道源码
 */
@TableName("mes_qc_indicator")
@KeySequence("mes_qc_indicator_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesQcIndicatorDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 检测项编码
     */
    private String code;
    /**
     * 检测项名称
     */
    private String name;
    /**
     * 检测项类型
     *
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.DictTypeConstants#MES_INDICATOR_TYPE}
     */
    private String type;
    /**
     * 检测工具
     */
    private String tool;
    /**
     * 结果值类型
     *
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.qc.MesQcResultValueTypeEnum}
     */
    private Integer resultType;
    /**
     * 结果值属性
     *
     * 1. FILE 时：存 IMG/FILE
     * 2. DICT 时：存字典类型名
     */
    private String resultSpecification;
    /**
     * 备注
     */
    private String remark;

}
