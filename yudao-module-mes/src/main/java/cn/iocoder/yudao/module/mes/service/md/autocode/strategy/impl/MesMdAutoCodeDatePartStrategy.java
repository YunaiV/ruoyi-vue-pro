package cn.iocoder.yudao.module.mes.service.md.autocode.strategy.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodePartDO;
import cn.iocoder.yudao.module.mes.enums.md.autocode.MesMdAutoCodePartTypeEnum;
import cn.iocoder.yudao.module.mes.service.md.autocode.strategy.MesMdAutoCodeContext;
import cn.iocoder.yudao.module.mes.service.md.autocode.strategy.MesMdAutoCodePartStrategy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MES 编码规则 - 当前日期策略
 *
 * @author 芋道源码
 */
@Component
public class MesMdAutoCodeDatePartStrategy implements MesMdAutoCodePartStrategy {

    @Override
    public Integer getType() {
        return MesMdAutoCodePartTypeEnum.DATE.getType();
    }

    @Override
    public String generate(MesMdAutoCodePartDO part, MesMdAutoCodeContext context) {
        String dateFormat = StrUtil.emptyToDefault(part.getDateFormat(), DatePattern.PURE_DATE_PATTERN);
        return DateUtil.format(LocalDateTime.now(), dateFormat);
    }

}
