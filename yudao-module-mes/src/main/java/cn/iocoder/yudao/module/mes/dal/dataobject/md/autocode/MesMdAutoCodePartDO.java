package cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MES 编码规则组成 DO
 *
 * @author 芋道源码
 */
@TableName("mes_md_auto_code_part")
@KeySequence("mes_md_auto_code_part_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesMdAutoCodePartDO extends BaseDO {

    /**
     * 分段 ID
     */
    @TableId
    private Long id;
    /**
     * 规则 ID
     *
     * 关联 {@link MesMdAutoCodeRuleDO#getId()}
     */
    private Long ruleId;
    /**
     * 分段序号
     */
    private Integer sort;
    /**
     * 分段长度
     */
    private Integer length;
    /**
     * 备注
     */
    private String remark;
    /**
     * 分段类型
     *
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.md.autocode.MesMdAutoCodePartTypeEnum}
     */
    private Integer type;

    // ========== 当 MesMdAutoCodePartTypeEnum#INPUT_CHAR 时，使用 ==========
    /**
     * 固定字符
     */
    private String fixCharacter;

    // ========== 当 MesMdAutoCodePartTypeEnum#DATE 时，使用 ==========
    /**
     * 日期格式
     *
     * 例如：yyyyMMdd、yyyyMM、HHmmss
     */
    private String dateFormat;

    // ========== 当 MesMdAutoCodePartTypeEnum#SERIAL_NUMBER 时，使用 ==========
    /**
     * 流水号起始值
     */
    private Integer serialStartNo;
    /**
     * 流水号步长
     */
    private Integer serialStep;
    /**
     * 流水号是否循环
     */
    private Boolean cycleFlag;
    /**
     * 循环方式
     *
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.md.autocode.MesMdAutoCodeCycleMethodEnum}
     */
    private Integer cycleMethod;

}
