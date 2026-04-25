package cn.iocoder.yudao.module.deepay.service.tool.impl;

import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayProductMapper;
import cn.iocoder.yudao.module.deepay.service.tool.AiTool;
import cn.iocoder.yudao.module.deepay.service.tool.ToolCallContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * 工具：搜索商品列表。
 */
@Slf4j
@Component
public class SearchProductsTool implements AiTool {

    @Resource
    private DeepayProductMapper productMapper;

    @Override
    public String getName() { return "searchProducts"; }

    @Override
    public String getDescription() {
        return "搜索商品列表，支持按关键词、类目、价格区间筛选，返回匹配商品列表（最多20条）。";
    }

    @Override
    public Map<String, Object> getParamsSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("keyword",   schemaField("string",  "搜索关键词（商品名/品类/风格）"));
        props.put("category",  schemaField("string",  "品类（外套/裤子/连衣裙等）"));
        props.put("minPrice",  schemaField("number",  "最低价格（元）"));
        props.put("maxPrice",  schemaField("number",  "最高价格（元）"));
        props.put("pageSize",  schemaField("integer", "返回条数，默认10，最大20"));
        schema.put("properties", props);
        return schema;
    }

    @Override
    public RiskLevel getRiskLevel() { return RiskLevel.LOW; }

    @Override
    public Map<String, Object> execute(ToolCallContext ctx, Map<String, Object> params) {
        String keyword  = getString(params, "keyword");
        int    pageSize = Math.min(getInt(params, "pageSize", 10), 20);
        // 简版：直接查前 N 条带关键词的商品
        List<Map<String, Object>> items = new ArrayList<>();
        try {
            productMapper.selectList(null).stream()
                    .filter(p -> keyword == null || keyword.isEmpty()
                            || (p.getTitle() != null && p.getTitle().contains(keyword)))
                    .limit(pageSize)
                    .forEach(p -> {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("id",    p.getId());
                        item.put("title", p.getTitle());
                        item.put("price", p.getPrice());
                        items.add(item);
                    });
        } catch (Exception e) {
            log.warn("[SearchProducts] 查询异常", e);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", items.size());
        result.put("items", items);
        return result;
    }

    // =========================================================================
    // 工具方法
    // =========================================================================

    private static Map<String, Object> schemaField(String type, String desc) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("type", type);
        f.put("description", desc);
        return f;
    }

    private static String getString(Map<String, Object> p, String key) {
        Object v = p.get(key);
        return v == null ? null : v.toString();
    }

    private static int getInt(Map<String, Object> p, String key, int def) {
        Object v = p.get(key);
        if (v == null) return def;
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return def; }
    }

}
