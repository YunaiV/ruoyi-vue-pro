package cn.iocoder.yudao.module.bpm.dal.redis;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelMetaInfoVO;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;

import static cn.hutool.core.date.DatePattern.*;

/**
 * BPM 流程 Id 编码的 Redis DAO
 *
 * @author Lesan
 */
@Repository
public class BpmProcessIdRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 生成序号，使用定义的 processIdRule 规则生成
     *
     * @param processIdRule 规则
     * @return 序号
     */
    public String generate(BpmModelMetaInfoVO.ProcessIdRule processIdRule) {
        // 生成日期前缀
        String infix = "";
        switch (processIdRule.getInfix()) {
            case "DAY":
                infix = DateUtil.format(LocalDateTime.now(), PURE_DATE_PATTERN);
                break;
            case "HOUR":
                infix = DateUtil.format(LocalDateTime.now(), PURE_DATE_PATTERN + "HH");
                break;
            case "MINUTE":
                infix = DateUtil.format(LocalDateTime.now(), PURE_DATE_PATTERN + "HHmm");
                break;
            case "SECOND":
                infix = DateUtil.format(LocalDateTime.now(), PURE_DATETIME_PATTERN);
                break;
        }

        // 生成序号
        String noPrefix = processIdRule.getPrefix() + infix + processIdRule.getPostfix();
        String key = RedisKeyConstants.BPM_PROCESS_ID + noPrefix;
        Long no = stringRedisTemplate.opsForValue().increment(key);
        if (StrUtil.isEmpty(infix)) {
            // 特殊：没有前缀，则不能过期，不能每次都是从 0 开始。可见 https://t.zsxq.com/MU1E2 讨论
            stringRedisTemplate.expire(key, Duration.ofDays(1L));
        }
        return noPrefix + String.format("%0" + processIdRule.getLength() + "d", no);
    }

}
