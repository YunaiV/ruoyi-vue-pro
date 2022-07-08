package cn.iocoder.yudao.module.infra.controller.admin.redis;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.redis.core.RedisKeyDefine;
import cn.iocoder.yudao.framework.redis.core.RedisKeyRegistry;
import cn.iocoder.yudao.module.infra.controller.admin.redis.vo.RedisKeyRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.redis.vo.RedisKeyValueRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.redis.vo.RedisMonitorRespVO;
import cn.iocoder.yudao.module.infra.convert.redis.RedisConvert;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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

    @GetMapping("/get-key-Defines")
    @ApiOperation("获得 Redis keys 键名列表")
    @PreAuthorize("@ss.hasPermission('infra:redis:get-key-list')")
    public CommonResult<Set<String>> getKeyDefines(@RequestParam("keyDefine") String keyDefine) {
        Set<String> keys = new HashSet<>();
         stringRedisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            try (Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions()
                    .match(keyDefine + "*")
                    .count(Integer.MAX_VALUE).build())) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return keys;
        });
        return success(keys);
    }

    @DeleteMapping("/delete-key-defines")
    @ApiOperation("删除 Redis Key 根据模板")
    @PreAuthorize("@ss.hasPermission('infra:redis:get-key-list')")
    public CommonResult<Boolean> deleteKeyDefines(@RequestParam("keyDefine") String keyDefine) {
        Set<String> keys = stringRedisTemplate.keys(keyDefine + "*");
        if(keys != null && keys.isEmpty()){
             stringRedisTemplate.delete(keys);
        }
        return success(Boolean.TRUE);
    }

    @GetMapping("/get-key-value")
    @ApiOperation("获得 Redis key 内容")
    @PreAuthorize("@ss.hasPermission('infra:redis:get-key-list')")
    public CommonResult<RedisKeyValueRespVO> getKeyValue(@RequestParam("keyDefine") String keyDefine, @RequestParam("cacheKey") String cacheKey) {
        String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
        return success(RedisKeyValueRespVO.of(keyDefine, cacheKey, cacheValue));
    }

    @DeleteMapping("/delete-key-value")
    @ApiOperation("删除 Redis Key 根据key")
    @PreAuthorize("@ss.hasPermission('infra:redis:get-key-list')")
    public CommonResult<Boolean> deleteKeyValue(@RequestParam("cacheKey") String cacheKey) {
        stringRedisTemplate.delete(cacheKey);
        return success(Boolean.TRUE);
    }

    @DeleteMapping("/delete-cache-all")
    @ApiOperation(value="删除 所有缓存", notes="不使用该接口")
    @PreAuthorize("@ss.hasPermission('infra:redis:get-key-list')")
    public CommonResult<Boolean> deleteCacheAll() {
        return success(stringRedisTemplate.execute((RedisCallback<Boolean>) connection -> {
            connection.flushAll();
            return Boolean.TRUE;
        }));
    }

}
