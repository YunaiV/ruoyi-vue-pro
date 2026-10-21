package cn.iocoder.yudao.framework.mybatis.core.util;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link CaseInsensitiveResultMap} 的单元测试
 *
 * @author 芋道源码
 */
public class CaseInsensitiveResultMapTest {

    @Test
    public void testGet_ignoreCase() {
        // 准备参数
        CaseInsensitiveResultMap<Object> map = new CaseInsensitiveResultMap<>();
        map.put("conversationId", 1024L);

        // 调用，并断言：PostgreSQL 小写、Oracle 大写，都可以取到值
        assertEquals(1024L, map.get("conversationId"));
        assertEquals(1024L, map.get("conversationid"));
        assertEquals(1024L, map.get("CONVERSATIONID"));
        assertTrue(map.containsKey("CONVERSATIONID"));
        assertEquals(1024L, MapUtil.getLong(map, "conversationid"));
        // 断言：保留原始的 key
        assertEquals(Collections.singletonList("conversationId"), new ArrayList<>(map.keySet()));
    }

    @Test
    public void testPut_nullValue() {
        // 准备参数
        CaseInsensitiveResultMap<Object> map = new CaseInsensitiveResultMap<>();
        map.put("sum_count", null);

        // 调用，并断言
        assertTrue(map.containsKey("SUM_COUNT"));
        assertNull(map.get("SUM_COUNT"));
        assertEquals(0, MapUtil.getInt(map, "SUM_COUNT", 0));
    }

    @Test
    public void testPut_sameKeyIgnoreCase() {
        // 准备参数
        CaseInsensitiveResultMap<Object> map = new CaseInsensitiveResultMap<>();

        // 调用：仅大小写不同的 key
        map.put("id", 1L);
        map.put("ID", 2L);

        // 断言：合并为一个，保留后写入的 key 和值
        assertEquals(1, map.size());
        assertEquals(2L, map.get("id"));
        assertEquals(Collections.singletonList("ID"), new ArrayList<>(map.keySet()));
    }

    @Test
    public void testGet_turkishDefaultLocale() {
        Locale defaultLocale = Locale.getDefault();
        try {
            // 准备参数：土耳其语下，大写 I 转小写为无点的 ı
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            CaseInsensitiveResultMap<Object> map = new CaseInsensitiveResultMap<>();
            map.put("CONVERSATIONID", 1024L);
            LinkedCaseInsensitiveMap<Object> defaultLocaleMap = new LinkedCaseInsensitiveMap<>();
            defaultLocaleMap.put("CONVERSATIONID", 1024L);

            // 调用，并断言：固定 Locale.ROOT，不受默认 Locale 影响
            assertEquals(1024L, map.get("conversationId"));
            // 断言：使用默认 Locale 时，取不到值
            assertNull(defaultLocaleMap.get("conversationId"));
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }

    @Test
    public void testConvertMap_numberKey() {
        // 准备参数：模拟 Oracle 返回的结果行
        List<Map<String, Object>> rows = Arrays.asList(buildRow(1024L, 3), buildRow(2048L, 5));

        // 调用：业务转换为数字 key 的 Map
        Map<Long, Integer> result = CollectionUtils.convertMap(rows,
                row -> MapUtil.getLong(row, "conversationId"),
                row -> MapUtil.getInt(row, "count"));

        // 断言：数字 key 的业务 Map 不受影响
        assertInstanceOf(HashMap.class, result);
        assertEquals(3, result.get(1024L));
        assertEquals(5, result.get(2048L));
    }

    private static Map<String, Object> buildRow(Long conversationId, Integer count) {
        CaseInsensitiveResultMap<Object> row = new CaseInsensitiveResultMap<>();
        row.put("CONVERSATIONID", conversationId);
        row.put("COUNT", count);
        return row;
    }

}
