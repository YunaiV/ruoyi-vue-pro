package cn.iocoder.yudao.module.bpm.dal.redis;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelMetaInfoVO;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;

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
                infix = DateUtil.format(LocalDateTime.now(), "yyyyMMDD");
                break;
            case "HOUR":
                infix = DateUtil.format(LocalDateTime.now(), "yyyyMMDDHH");
                break;
            case "MINUTE":
                infix = DateUtil.format(LocalDateTime.now(), "yyyyMMDDHHmm");
                break;
            case "SECOND":
                infix = DateUtil.format(LocalDateTime.now(), "yyyyMMDDHHmmss");
                break;
        }

        // 生成序号
        String noPrefix = processIdRule.getPrefix() + infix + processIdRule.getPostfix();
        String key = RedisKeyConstants.BPM_PROCESS_ID + noPrefix;
        Long no = stringRedisTemplate.opsForValue().increment(key);
        stringRedisTemplate.expire(key, Duration.ofDays(1L));
        return noPrefix + String.format("%0" + processIdRule.getLength() + "d", no);
    }

}
