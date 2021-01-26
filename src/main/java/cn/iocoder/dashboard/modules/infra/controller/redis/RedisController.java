package cn.iocoder.dashboard.modules.infra.controller.redis;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.framework.redis.core.RedisKeyRegistry;
import cn.iocoder.dashboard.modules.infra.controller.redis.vo.InfRedisKeyRespVO;
import cn.iocoder.dashboard.modules.infra.controller.redis.vo.InfRedisMonitorRespVO;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/infra/redis")
public class RedisController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

//    @PreAuthorize("@ss.hasPermission('infra:redis:get-monitor-info')")
    @GetMapping("/get-monitor-info")
    public CommonResult<InfRedisMonitorRespVO> getRedisMonitorInfo() {
        // 获得 Redis 统计信息
        Properties info = stringRedisTemplate.execute((RedisCallback<Properties>) RedisServerCommands::info);
        Long dbSize = stringRedisTemplate.execute(RedisServerCommands::dbSize);
        Properties commandStats = stringRedisTemplate.execute((
                RedisCallback<Properties>) connection -> connection.info("commandstats"));
        assert commandStats != null; // 断言，避免警告

        // 拼接结果返回
        InfRedisMonitorRespVO respVO = InfRedisMonitorRespVO.builder().info(info).dbSize(dbSize)
                .commandStats(new ArrayList<>(commandStats.size())).build();
        commandStats.forEach((key, value) -> {
            respVO.getCommandStats().add(InfRedisMonitorRespVO.CommandStat.builder()
                    .command(StrUtil.subAfter((String) key, "cmdstat_", false))
                    .calls(Integer.valueOf(StrUtil.subBetween((String) value, "calls=", ",")))
                    .usec(Integer.valueOf(StrUtil.subBetween((String) value, "usec=", ",")))
                    .build());
        });
        return success(respVO);
    }

//    @PreAuthorize("@ss.hasPermission('infra:redis:get-key-list')")
    @GetMapping("/get-key-list")
    public CommonResult<List<InfRedisKeyRespVO>> getKeyList() {
        List<InfRedisKeyRespVO> respVOList = RedisKeyRegistry.list().stream()
                .map(define -> InfRedisKeyRespVO.builder()
                        .keyTemplate(define.getKeyTemplate())
                        .keyType(define.getKeyType().name())
                        .valueType(define.getValueType().getName())
                        .timeoutType(define.getTimeoutType().getType())
                        .timeout((int) define.getTimeout().getSeconds())
                        .build())
                .collect(Collectors.toList());
        return success(respVOList);
    }

}
