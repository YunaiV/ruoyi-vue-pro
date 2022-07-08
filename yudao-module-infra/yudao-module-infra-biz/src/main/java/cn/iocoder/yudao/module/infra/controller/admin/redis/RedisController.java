package cn.iocoder.yudao.module.infra.controller.admin.redis;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.redis.core.RedisKeyDefine;
import cn.iocoder.yudao.framework.redis.core.RedisKeyRegistry;
import cn.iocoder.yudao.module.infra.controller.admin.redis.vo.RedisKeyDefineRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.redis.vo.RedisKeyValueRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.redis.vo.RedisMonitorRespVO;
import cn.iocoder.yudao.module.infra.convert.redis.RedisConvert;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
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

    @GetMapping("/get-key-define-list")
    @ApiOperation("获得 Redis Key 模板列表")
    @PreAuthorize("@ss.hasPermission('infra:redis:get-key-list')")
    public CommonResult<List<RedisKeyDefineRespVO>> getKeyDefineList() {
        List<RedisKeyDefine> keyDefines = RedisKeyRegistry.list();
        return success(RedisConvert.INSTANCE.convertList(keyDefines));
    }

    @GetMapping("/get-key-list")
    @ApiOperation("获得 Redis keys 键名列表")
    @ApiImplicitParam(name = "keyTemplate", value = "Redis Key 定义", example = "true", dataTypeClass = String.class)
    @PreAuthorize("@ss.hasPermission('infra:redis:get-key-list')")
    public CommonResult<Set<String>> getKeyDefineList(@RequestParam("keyTemplate") String keyTemplate) {
        return success(getKeyDefineList0(keyTemplate));
    }

    private Set<String> getKeyDefineList0(String keyTemplate) {
        // key 格式化
        String key = StrUtil.replace(keyTemplate, "%[s|c|b|d|x|o|f|a|e|g]", parameter -> "*");
        // scan 扫描 key
        Set<String> keys = new LinkedHashSet<>();
        stringRedisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            try (Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(key).count(100).build())) {
                cursor.forEachRemaining(value -> keys.add(StrUtil.utf8Str(value)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return keys;
        });
        return keys;
    }

    @GetMapping("/get-key-value")
    @ApiOperation("获得 Redis key 内容")
    @ApiImplicitParam(name = "key", value = "Redis Key", example = "oauth2_access_token:233", dataTypeClass = String.class)
    @PreAuthorize("@ss.hasPermission('infra:redis:get-key-list')")
    public CommonResult<RedisKeyValueRespVO> getKeyValue(@RequestParam("key") String key) {
        String value = stringRedisTemplate.opsForValue().get(key);
        return success(new RedisKeyValueRespVO(key, value));
    }

    @DeleteMapping("/delete-key")
    @ApiOperation("删除 Redis Key")
    @ApiImplicitParam(name = "key", value = "Redis Key", example = "oauth2_access_token:233", dataTypeClass = String.class)
    @PreAuthorize("@ss.hasPermission('infra:redis:get-key-list')")
    public CommonResult<Boolean> deleteKey(@RequestParam("key") String key) {
        stringRedisTemplate.delete(key);
        return success(Boolean.TRUE);
    }

    @DeleteMapping("/delete-keys")
    @ApiOperation("删除 Redis Key 根据模板")
    @ApiImplicitParam(name = "keyTemplate", value = "Redis Key 定义", example = "true", dataTypeClass = String.class)
    @PreAuthorize("@ss.hasPermission('infra:redis:get-key-list')")
    public CommonResult<Boolean> deleteKeys(@RequestParam("keyTemplate") String keyTemplate) {
        Set<String> keys = getKeyDefineList0(keyTemplate);
        if (CollUtil.isNotEmpty(keys)) {
            stringRedisTemplate.delete(keys);
        }
        return success(Boolean.TRUE);
    }

}
