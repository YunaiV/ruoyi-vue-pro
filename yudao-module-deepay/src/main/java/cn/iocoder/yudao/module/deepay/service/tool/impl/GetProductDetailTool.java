package cn.iocoder.yudao.module.deepay.service.tool.impl;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayProductDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayProductMapper;
import cn.iocoder.yudao.module.deepay.service.tool.AiTool;
import cn.iocoder.yudao.module.deepay.service.tool.ToolCallContext;
import cn.iocoder.yudao.module.deepay.service.tool.ToolExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 工具：获取商品详情。
 */
@Slf4j
@Component
public class GetProductDetailTool implements AiTool {

    @Resource
    private DeepayProductMapper productMapper;

    @Override public String getName() { return "getProductDetail"; }

    @Override
    public String getDescription() {
        return "按商品ID获取商品详细信息，包含标题、描述、价格、封面图等。";
    }

    @Override
    public Map<String, Object> getParamsSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("productId", field("integer", "商品ID（必填）"));
        schema.put("properties", props);
        schema.put("required", new String[]{"productId"});
        return schema;
    }

    @Override public RiskLevel getRiskLevel() { return RiskLevel.LOW; }

    @Override
    public Map<String, Object> execute(ToolCallContext ctx, Map<String, Object> params) {
        Long productId = getLong(params, "productId");
        if (productId == null) {
            throw new ToolExecutionException(getName(), "MISSING_PARAM", "缺少 productId");
        }
        DeepayProductDO p = productMapper.selectById(productId);
        if (p == null) {
            throw new ToolExecutionException(getName(), "NOT_FOUND", "商品不存在: " + productId);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id",          p.getId());
        result.put("title",       p.getTitle());
        result.put("description", p.getDescription());
        result.put("price",       p.getPrice());
        result.put("coverImage",  p.getCoverImage());
        return result;
    }

    private static Map<String, Object> field(String type, String desc) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("type", type); f.put("description", desc); return f;
    }

    private static Long getLong(Map<String, Object> p, String key) {
        Object v = p.get(key);
        if (v == null) return null;
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }

}
