package cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.enums.md.autocode.MesMdAutoCodePartTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MES 编码生成记录 DO
 *
 * @author 芋道源码
 */
@TableName("mes_md_auto_code_record")
@KeySequence("mes_md_auto_code_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesMdAutoCodeRecordDO extends BaseDO {

    /**
     * 记录 ID
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
     * 生成的编码
     */
    private String result;
    /**
     * 传入的参数
     */
    private String inputChar;
    /**
     * 生成的流水号
     *
     * 当规则组成中包含流水号分段（{@link MesMdAutoCodePartTypeEnum#SERIAL_NUMBER}）时记录，方便追溯和调试
     */
    private Long serialNo;

}
