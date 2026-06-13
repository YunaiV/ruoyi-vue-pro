package cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 编码规则 DO
 *
 * @author 芋道源码
 */
@TableName("mes_md_auto_code_rule")
@KeySequence("mes_md_auto_code_rule_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesMdAutoCodeRuleDO extends BaseDO {

    /**
     * 规则 ID
     */
    @TableId
    private Long id;
    /**
     * 规则编码
     */
    private String code;
    /**
     * 规则名称
     */
    private String name;
    /**
     * 描述
     */
    private String description;
    /**
     * 最大长度
     */
    private Integer maxLength;
    /**
     * 是否补齐
     */
    private Boolean padded;
    /**
     * 补齐字符
     */
    private String paddedChar;
    /**
     * 补齐方式
     *
     * 字典 {@link DictTypeConstants#MES_MD_AUTO_CODE_PADDED_METHOD}
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.md.autocode.MesMdAutoCodePaddedMethodEnum}
     */
    private Integer paddedMethod;
    /**
     * 状态
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
