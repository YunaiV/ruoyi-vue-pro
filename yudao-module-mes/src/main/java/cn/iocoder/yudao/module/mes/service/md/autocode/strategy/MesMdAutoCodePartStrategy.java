package cn.iocoder.yudao.module.mes.service.md.autocode.strategy;

import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodePartDO;

/**
 * MES 编码规则分段策略接口
 *
 * @author 芋道源码
 */
public interface MesMdAutoCodePartStrategy {

    /**
     * 获取策略类型
     *
     * @return 分段类型
     */
    Integer getType();

    /**
     * 生成分段内容
     *
     * @param part 规则组成
     * @param context 上下文
     * @return 分段内容
     */
    String generate(MesMdAutoCodePartDO part, MesMdAutoCodeContext context);

}
