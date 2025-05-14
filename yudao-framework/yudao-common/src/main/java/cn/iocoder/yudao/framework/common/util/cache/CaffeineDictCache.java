package cn.iocoder.yudao.framework.common.util.cache;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.springframework.jdbc.core.JdbcTemplate;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 基于 Caffeine 实现的字典缓存
 *
 * 1. 支持随机过期时间，避免缓存雪崩 2. 支持缓存空值，避免缓存穿透 3. 支持批量查询和预热
 *
 * @author Ken
 */
@Slf4j
public class CaffeineDictCache {

    private static volatile JdbcTemplate jdbcTemplate;
    private static volatile String dictQuerySql;

    /**
     * 缓存过期时间下限（分钟）
     */
    private static final int EXPIRE_TIME_MIN = 120;

    /**
     * 缓存过期时间抖动范围（分钟）
     */
    private static final int EXPIRE_TIME_RANGE = 60;

    /**
     * 空值缓存过期时间（分钟）
     */
    private static final int EMPTY_VALUE_EXPIRE_TIME = 5;

    /**
     * SQL语句缓存，避免重复解析
     */
    private static final ConcurrentHashMap<String, String> SQL_CACHE = new ConcurrentHashMap<>();

    /**
     * 正常值的缓存
     */
    private static final Cache<String, List<String>> NORMAL_DICT_CACHE = Caffeine.newBuilder()
            // 初始容量
            .initialCapacity(128)
            // 最大容量，超过会自动清理
            .maximumSize(1024)
            // 默认2小时加抖动，120 + [0, 60] 分钟，避免缓存雪崩
            .expireAfterWrite(getDynamicExpireTime(), TimeUnit.MINUTES)
            // 记录命中率等指标
            .recordStats().build();

    /**
     * 空值的缓存
     */
    private static final Cache<String, List<String>> EMPTY_DICT_CACHE = Caffeine.newBuilder()
            // 初始容量
            .initialCapacity(32)
            // 最大容量，超过会自动清理
            .maximumSize(512)
            // 空值使用较短的过期时间
            .expireAfterWrite(EMPTY_VALUE_EXPIRE_TIME, TimeUnit.MINUTES)
            // 记录命中率等指标
            .recordStats().build();

    public static void setDictQuerySql(String sql) {
        dictQuerySql = sql;
    }

    private static JdbcTemplate getJdbcTemplate() {
        if (jdbcTemplate == null) {
            synchronized (CaffeineDictCache.class) {
                if (jdbcTemplate == null) {
                    jdbcTemplate = SpringUtils.getBean(JdbcTemplate.class);
                }
            }
        }
        return jdbcTemplate;
    }

    /**
     * 获取动态过期时间，避免缓存雪崩
     */
    private static long getDynamicExpireTime() {
        return EXPIRE_TIME_MIN + ThreadLocalRandom.current().nextInt(EXPIRE_TIME_RANGE);
    }

    /**
     * 生成缓存键
     */
    private static String generateCacheKey(String dictType, String sql) {
        return StrUtil.isNotEmpty(sql) ? sql + "#" + dictType : dictType;
    }

    /**
     * 获取字典值列表
     *
     * @param dictType 字典类型
     * @param sql SQL语句
     * @return 字典值列表
     */
    public static List<String> getDictValues(String dictType, String sql) {
        if (dictType == null) {
            return Collections.emptyList();
        }

        String cacheKey = generateCacheKey(dictType, sql);

        // 先从正常缓存中获取
        List<String> dictValues = NORMAL_DICT_CACHE.getIfPresent(cacheKey);
        if (dictValues != null) {
            return dictValues;
        }

        // 再从空值缓存中获取
        dictValues = EMPTY_DICT_CACHE.getIfPresent(cacheKey);
        if (dictValues != null) {
            return dictValues;
        }

        // 缓存未命中，从数据库加载
        return loadAndCacheDictValues(dictType, sql, cacheKey);
    }

    /**
     * 批量获取字典值列表
     *
     * @param dictTypes 字典类型列表
     * @param sql SQL语句
     * @return 字典值Map
     */
    public static Map<String, List<String>> getDictValuesMap(Collection<String> dictTypes,
            String sql) {
        if (CollUtil.isEmpty(dictTypes)) {
            return Collections.emptyMap();
        }

        Map<String, List<String>> result = new HashMap<>(dictTypes.size());
        Set<String> missedTypes = new HashSet<>();

        // 先从缓存获取
        for (String dictType : dictTypes) {
            String cacheKey = generateCacheKey(dictType, sql);
            // 先从正常缓存中获取
            List<String> values = NORMAL_DICT_CACHE.getIfPresent(cacheKey);
            if (values != null) {
                result.put(dictType, values);
                continue;
            }
            // 再从空值缓存中获取
            values = EMPTY_DICT_CACHE.getIfPresent(cacheKey);
            if (values != null) {
                result.put(dictType, values);
                continue;
            }
            missedTypes.add(dictType);
        }

        // 查询未命中的数据
        if (!missedTypes.isEmpty()) {
            for (String dictType : missedTypes) {
                String cacheKey = generateCacheKey(dictType, sql);
                List<String> values = loadAndCacheDictValues(dictType, sql, cacheKey);
                result.put(dictType, values);
            }
        }

        return result;
    }

    /**
     * 加载并缓存字典值
     */
    private static List<String> loadAndCacheDictValues(String dictType, String sql,
            String cacheKey) {
        try {
            List<String> dbValues = getJdbcTemplate().queryForList(
                    SQL_CACHE.computeIfAbsent(sql, k -> dictQuerySql), new Object[] {dictType},
                    String.class);

            List<String> values = dbValues != null ? dbValues : Collections.emptyList();
            // 根据是否为空选择不同的缓存
            if (values.isEmpty()) {
                EMPTY_DICT_CACHE.put(cacheKey, values);
            } else {
                NORMAL_DICT_CACHE.put(cacheKey, values);
            }
            return values;
        } catch (Exception e) {
            log.error("获取字典值异常, dictType: {}", dictType, e);
            List<String> emptyList = Collections.emptyList();
            EMPTY_DICT_CACHE.put(cacheKey, emptyList);
            return emptyList;
        }
    }

    /**
     * 类型转换比较
     *
     * @param dbValue 数据库值
     * @param targetValue 目标值
     * @return 是否匹配
     */
    public static boolean compareValue(String dbValue, Object targetValue) {
        if (targetValue == null || dbValue == null) {
            return false;
        }

        // 数字类型特殊处理
        if (targetValue instanceof Number) {
            try {
                return new BigDecimal(dbValue)
                        .compareTo(new BigDecimal(targetValue.toString())) == 0;
            } catch (Exception ignore) {
                // 转换失败，回退到字符串比较
            }
        }
        return dbValue.equals(targetValue.toString());
    }

    /**
     * 预热缓存
     *
     * @param dictTypes 字典类型列表
     * @param sql SQL语句
     */
    public static void warmup(Collection<String> dictTypes, String sql) {
        if (CollUtil.isEmpty(dictTypes)) {
            return;
        }
        getDictValuesMap(dictTypes, sql);
    }

    /**
     * 清除指定类型的缓存
     */
    public static void invalidateCache(String dictType, String sql) {
        if (dictType != null) {
            String cacheKey = generateCacheKey(dictType, sql);
            NORMAL_DICT_CACHE.invalidate(cacheKey);
            EMPTY_DICT_CACHE.invalidate(cacheKey);
        }
    }

    /**
     * 批量清除缓存
     */
    public static void invalidateCaches(Collection<String> dictTypes, String sql) {
        if (CollUtil.isEmpty(dictTypes)) {
            return;
        }
        dictTypes.forEach(dictType -> invalidateCache(dictType, sql));
    }

    /**
     * 清除所有缓存
     */
    public static void invalidateAll() {
        NORMAL_DICT_CACHE.invalidateAll();
        EMPTY_DICT_CACHE.invalidateAll();
        SQL_CACHE.clear();
    }

    /**
     * 获取缓存统计信息
     */
    public static String getCacheStats() {
        return String.format("Normal Cache Stats: %s\nEmpty Cache Stats: %s",
                NORMAL_DICT_CACHE.stats().toString(), EMPTY_DICT_CACHE.stats().toString());
    }
}
