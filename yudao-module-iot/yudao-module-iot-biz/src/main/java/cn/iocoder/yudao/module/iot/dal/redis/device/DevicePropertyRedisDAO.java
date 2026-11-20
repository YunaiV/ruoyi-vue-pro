package cn.iocoder.yudao.module.iot.dal.redis.device;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants.DEVICE_PROPERTY;

/**
 * {@link IotDevicePropertyDO} 的 Redis DAO
 */
@Repository
public class DevicePropertyRedisDAO {

    /**
     * 只覆盖更早的值的脚本：ARGV 每 3 个一组，依次为属性标识、属性 JSON、更新时间（毫秒）；
     * 已有的值更新时间更晚时不覆盖，已有的值无法解析时覆盖
     */
    private static final RedisScript<Long> PUT_ALL_IF_LATER_SCRIPT = new DefaultRedisScript<>(
            "for i = 1, #ARGV, 3 do\n"
                    + "    local old = redis.call('HGET', KEYS[1], ARGV[i])\n"
                    + "    local oldTime = nil\n"
                    + "    if old then\n"
                    + "        local ok, value = pcall(cjson.decode, old)\n"
                    + "        if ok and type(value) == 'table' then\n"
                    + "            oldTime = tonumber(value['updateTime'])\n"
                    + "        end\n"
                    + "    end\n"
                    + "    if not oldTime or oldTime <= tonumber(ARGV[i + 2]) then\n"
                    + "        redis.call('HSET', KEYS[1], ARGV[i], ARGV[i + 1])\n"
                    + "    end\n"
                    + "end\n"
                    + "return 1\n", Long.class);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public Map<String, IotDevicePropertyDO> get(Long id) {
        String redisKey = formatKey(id);
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(redisKey);
        if (CollUtil.isEmpty(entries)) {
            return Collections.emptyMap();
        }
        return convertMap(entries.entrySet(),
                entry -> (String) entry.getKey(),
                entry -> JsonUtils.parseObject((String) entry.getValue(), IotDevicePropertyDO.class));
    }

    public void putAll(Long id, Map<String, IotDevicePropertyDO> properties) {
        if (CollUtil.isEmpty(properties)) {
            return;
        }
        String redisKey = formatKey(id);
        stringRedisTemplate.opsForHash().putAll(redisKey, convertMap(properties.entrySet(),
                Map.Entry::getKey,
                entry -> JsonUtils.toJsonString(entry.getValue())));
    }

    /**
     * 写入属性：只覆盖更早的值，不覆盖更新时间更晚的值（补传的消息使用）
     *
     * @param id         设备编号
     * @param properties 属性，updateTime 不能为空
     */
    public void putAllIfLater(Long id, Map<String, IotDevicePropertyDO> properties) {
        if (CollUtil.isEmpty(properties)) {
            return;
        }
        // 1. 构建脚本参数：每个属性依次为「标识符、属性 JSON、更新时间（毫秒）」，脚本按 3 个一组读取
        List<String> args = new ArrayList<>(properties.size() * 3);
        properties.forEach((identifier, property) -> {
            args.add(identifier);
            args.add(JsonUtils.toJsonString(property));
            args.add(String.valueOf(LocalDateTimeUtil.toEpochMilli(property.getUpdateTime())));
        });
        // 2. 执行脚本：原子地比较、写入，只覆盖不存在、无法解析、或更新时间不晚于本次的属性
        stringRedisTemplate.execute(PUT_ALL_IF_LATER_SCRIPT, Collections.singletonList(formatKey(id)),
                args.toArray());
    }

    private static String formatKey(Long id) {
        return String.format(DEVICE_PROPERTY, id);
    }

}
