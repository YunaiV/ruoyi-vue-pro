package cn.iocoder.yudao.module.mes.service.md.autocode.strategy;

import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodePartDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodeRuleDO;
import lombok.Data;

import java.util.List;

/**
 * MES 编码生成上下文
 *
 * @author 芋道源码
 */
@Data
public class MesMdAutoCodeContext {

    /**
     * 规则
     */
    private MesMdAutoCodeRuleDO rule;
    /**
     * 规则组成列表
     */
    private List<MesMdAutoCodePartDO> parts;

    /**
     * 输入字符
     */
    private String inputChar;

    /**
     * 流水号
     *
     * 由流水号策略生成并设置，用于保存到生成记录表。例如：当规则包含流水号分段时，生成编码 "ITEM_20260304_0001"，此字段值为 1
     */
    private Long serialNo;

}
