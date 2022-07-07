package cn.iocoder.yudao.module.infra.controller.admin.redis;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.redis.core.RedisKeyDefine;
import cn.iocoder.yudao.framework.redis.core.RedisKeyRegistry;
import cn.iocoder.yudao.module.infra.controller.admin.redis.vo.RedisKeyRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.redis.vo.RedisMonitorRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.redis.vo.RedisValuesRespVO;
import cn.iocoder.yudao.module.infra.convert.redis.RedisConvert;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

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

    @GetMapping("/get-key/{keyDefine}")
    @ApiOperation("获得 Redis Key")
//    @PreAuthorize("@ss.hasPermission('infra:redis:get-key-list')")
    public CommonResult<Set<String>> getKeyDefineKeys(@PathVariable("keyDefine") String keyDefine) {
        Set<String> Keys = stringRedisTemplate.keys(keyDefine + "*");
        return success(Keys);
    }

    @DeleteMapping("/clear-key/{keyDefine}")
    @ApiOperation("获得 Redis Key")
//    @PreAuthorize("@ss.hasPermission('infra:redis:get-key-list')")
    public CommonResult<Boolean> clearKeyDefineKeys(@PathVariable("keyDefine") String keyDefine) {
        stringRedisTemplate.delete(Objects.requireNonNull(stringRedisTemplate.keys(keyDefine + "*")));
        return success(Boolean.TRUE);
    }

//    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @GetMapping("/get-key/{cacheName}/{cacheKey}")
    public CommonResult<RedisValuesRespVO> getKeyValue(@PathVariable("cacheName") String cacheName, @PathVariable("cacheKey") String cacheKey) {
        String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
        return success(new RedisValuesRespVO(cacheName, cacheKey, cacheValue));
    }

//    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @DeleteMapping("/clearCacheKey/{cacheKey}")
    public CommonResult<Boolean> clearCacheKey(@PathVariable String cacheKey) {
        stringRedisTemplate.delete(cacheKey);
        return success(Boolean.TRUE);
    }

//    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @DeleteMapping("/clearCacheAll")
    public CommonResult<Boolean> clearCacheAll() {
        Collection<String> cacheKeys = stringRedisTemplate.keys("*");
        stringRedisTemplate.delete(cacheKeys);
        return success(Boolean.TRUE);
    }

}
