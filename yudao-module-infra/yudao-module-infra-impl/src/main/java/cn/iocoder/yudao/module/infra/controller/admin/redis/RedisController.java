package cn.iocoder.yudao.module.infra.controller.admin.redis;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.redis.core.RedisKeyDefine;
import cn.iocoder.yudao.framework.redis.core.RedisKeyRegistry;
import cn.iocoder.yudao.module.infra.controller.admin.redis.vo.RedisKeyRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.redis.vo.RedisMonitorRespVO;
import cn.iocoder.yudao.module.infra.convert.redis.RedisConvert;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Properties;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "管理后台 - Redis 监控")
@RestController
@RequestMapping("/infra/redis")
public class RedisController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/get-monitor-info")
    @ApiOperation("获得 Redis 监控信息")
    @PreAuthorize("@ss.hasPermission('infra:redis:get-monitor-info')")
    public CommonResult<RedisMonitorRespVO> getRedisMonitorInfo() {
        // 获得 Redis 统计信息
        Properties info = stringRedisTemplate.execute((RedisCallback<Properties>) RedisServerCommands::info);
        Long dbSize = stringRedisTemplate.execute(RedisServerCommands::dbSize);
        Properties commandStats = stringRedisTemplate.execute((
                RedisCallback<Properties>) connection -> connection.info("commandstats"));
        assert commandStats != null; // 断言，避免警告
        // 拼接结果返回
        return success(RedisConvert.INSTANCE.build(info, dbSize, commandStats));
    }

    @GetMapping("/get-key-list")
    @ApiOperation("获得 Redis Key 列表")
    @PreAuthorize("@ss.hasPermission('infra:redis:get-key-list')")
    public CommonResult<List<RedisKeyRespVO>> getKeyList() {
        List<RedisKeyDefine> keyDefines = RedisKeyRegistry.list();
        return success(RedisConvert.INSTANCE.convertList(keyDefines));
    }

}
