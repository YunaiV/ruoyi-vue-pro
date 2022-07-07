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
    @ApiOperation("获得 Redis Key 模板列表")
    @PreAuthorize("@ss.hasPermission('infra:redis:get-key-list')")
    public CommonResult<List<RedisKeyRespVO>> getKeyList() {
        List<RedisKeyDefine> keyDefines = RedisKeyRegistry.list();
        return success(RedisConvert.INSTANCE.convertList(keyDefines));
    }

    @GetMapping("/get-key/{keyDefine}")
    @ApiOperation("获得 Redis keys 键名列表")
    @PreAuthorize("@ss.hasPermission('infra:redis:get-key-list')")
    public CommonResult<Set<String>> getKeyDefineKeys(@PathVariable("keyDefine") String keyDefine) {
        Set<String> Keys = stringRedisTemplate.keys(keyDefine + "*");
        return success(Keys);
    }

    @DeleteMapping("/clear-key-define/{keyDefine}")
    @ApiOperation("删除 Redis Key 根据模板")
    @PreAuthorize("@ss.hasPermission('infra:redis:get-key-list')")
    public CommonResult<Boolean> clearKeyDefineKeys(@PathVariable("keyDefine") String keyDefine) {
        stringRedisTemplate.delete(Objects.requireNonNull(stringRedisTemplate.keys(keyDefine + "*")));
        return success(Boolean.TRUE);
    }

    @GetMapping("/get-key/{keyDefine}/{cacheKey}")
    @ApiOperation("获得 Redis key 内容")
    @PreAuthorize("@ss.hasPermission('infra:redis:get-key-list')")
    public CommonResult<RedisValuesRespVO> getKeyValue(@PathVariable("keyDefine") String keyDefine, @PathVariable("cacheKey") String cacheKey) {
        String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
        return success(new RedisValuesRespVO(keyDefine, cacheKey, cacheValue));
    }

    @DeleteMapping("/clear-key/{cacheKey}")
    @ApiOperation("删除 Redis Key 根据key")
    @PreAuthorize("@ss.hasPermission('infra:redis:get-key-list')")
    public CommonResult<Boolean> clearCacheKey(@PathVariable String cacheKey) {
        stringRedisTemplate.delete(cacheKey);
        return success(Boolean.TRUE);
    }

    @DeleteMapping("/clear-cache-all")
    @ApiOperation(value="删除 所有缓存", notes="不使用该接口")
    @PreAuthorize("@ss.hasPermission('infra:redis:get-key-list')")
    public CommonResult<Boolean> clearCacheAll() {
        Collection<String> cacheKeys = stringRedisTemplate.keys("*");
        stringRedisTemplate.delete(cacheKeys);
        return success(Boolean.TRUE);
    }

}
