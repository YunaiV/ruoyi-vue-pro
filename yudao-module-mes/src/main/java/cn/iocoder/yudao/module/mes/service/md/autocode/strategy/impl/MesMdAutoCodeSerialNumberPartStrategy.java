package cn.iocoder.yudao.module.mes.service.md.autocode.strategy.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodePartDO;
import cn.iocoder.yudao.module.mes.dal.redis.md.autocode.MesMdAutoCodeRedisDAO;
import cn.iocoder.yudao.module.mes.enums.md.autocode.MesMdAutoCodeCycleMethodEnum;
import cn.iocoder.yudao.module.mes.enums.md.autocode.MesMdAutoCodePartTypeEnum;
import cn.iocoder.yudao.module.mes.service.md.autocode.strategy.MesMdAutoCodeContext;
import cn.iocoder.yudao.module.mes.service.md.autocode.strategy.MesMdAutoCodePartStrategy;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * MES 编码规则 - 流水号策略
 *
 * @author 芋道源码
 */
@Component
public class MesMdAutoCodeSerialNumberPartStrategy implements MesMdAutoCodePartStrategy {

    @Resource
    private MesMdAutoCodeRedisDAO redisDAO;

    @Override
    public Integer getType() {
        return MesMdAutoCodePartTypeEnum.SERIAL_NUMBER.getType();
    }

    @Override
    public String generate(MesMdAutoCodePartDO part, MesMdAutoCodeContext context) {
        // 1.1 构建 Redis key 后缀
        String keySuffix = buildRedisKeySuffix(part, context);
        // 1.2 获取过期时间
        Duration duration = Boolean.TRUE.equals(part.getCycleFlag()) ? getExpireDuration(part.getCycleMethod()) : null;

        // 2. 获取流水号
        Integer step = part.getSerialStep() != null ? part.getSerialStep() : 1;
        Integer startNo = part.getSerialStartNo() != null ? part.getSerialStartNo() : 1;
        Long serialNo = redisDAO.increment(keySuffix, duration, startNo, step);

        // 3. 格式化输出（补零）
        String serialStr = String.valueOf(serialNo);
        if (serialStr.length() < part.getLength()) {
            serialStr = StrUtil.padPre(serialStr, part.getLength(), '0');
        }
        context.setSerialNo(serialNo);
        return serialStr;
    }

    /**
     * 构建 Redis key 后缀
     */
    private String buildRedisKeySuffix(MesMdAutoCodePartDO part, MesMdAutoCodeContext context) {
        StringBuilder keySuffix = new StringBuilder();
        keySuffix.append(context.getRule().getId());

        // 如果不循环，直接返回
        if (!Boolean.TRUE.equals(part.getCycleFlag()) || part.getCycleMethod() == null) {
            return keySuffix.toString();
        }

        // 根据循环方式构建 key 后缀
        keySuffix.append(":");
        LocalDateTime time = LocalDateTime.now();
        if (part.getCycleMethod().equals(MesMdAutoCodeCycleMethodEnum.YEAR.getMethod())) {
            keySuffix.append(DateUtil.format(time, "yyyy"));
        } else if (part.getCycleMethod().equals(MesMdAutoCodeCycleMethodEnum.MONTH.getMethod())) {
            keySuffix.append(DateUtil.format(time, "yyyyMM"));
        } else if (part.getCycleMethod().equals(MesMdAutoCodeCycleMethodEnum.DAY.getMethod())) {
            keySuffix.append(DateUtil.format(time, "yyyyMMdd"));
        } else if (part.getCycleMethod().equals(MesMdAutoCodeCycleMethodEnum.HOUR.getMethod())) {
            keySuffix.append(DateUtil.format(time, "yyyyMMddHH"));
        } else if (part.getCycleMethod().equals(MesMdAutoCodeCycleMethodEnum.MINUTE.getMethod())) {
            keySuffix.append(DateUtil.format(time, "yyyyMMddHHmm"));
        } else if (part.getCycleMethod().equals(MesMdAutoCodeCycleMethodEnum.INPUT_CHAR.getMethod())) {
            if (StrUtil.isNotEmpty(context.getInputChar())) {
                keySuffix.append(context.getInputChar());
            }
        } else {
            throw new IllegalArgumentException("未知的循环方式：" + part.getCycleMethod());
        }
        return keySuffix.toString();
    }

    /**
     * 获取过期时间
     */
    private Duration getExpireDuration(Integer cycleMethod) {
        if (cycleMethod == null) {
            return Duration.ofDays(1);
        }
        if (cycleMethod.equals(MesMdAutoCodeCycleMethodEnum.YEAR.getMethod())) {
            return Duration.ofDays(366);
        } else if (cycleMethod.equals(MesMdAutoCodeCycleMethodEnum.MONTH.getMethod())) {
            return Duration.ofDays(32);
        } else if (cycleMethod.equals(MesMdAutoCodeCycleMethodEnum.DAY.getMethod())) {
            return Duration.ofDays(2);
        } else if (cycleMethod.equals(MesMdAutoCodeCycleMethodEnum.HOUR.getMethod())) {
            return Duration.ofHours(2);
        } else if (cycleMethod.equals(MesMdAutoCodeCycleMethodEnum.MINUTE.getMethod())) {
            return Duration.ofMinutes(2);
        } else if (cycleMethod.equals(MesMdAutoCodeCycleMethodEnum.INPUT_CHAR.getMethod())) {
            // 永不过期
            return null;
        } else {
            throw new IllegalArgumentException("未知的循环方式：" + cycleMethod);
        }
    }

}
