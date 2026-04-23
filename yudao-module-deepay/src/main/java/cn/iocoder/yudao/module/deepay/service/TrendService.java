package cn.iocoder.yudao.module.deepay.service;

import java.util.List;

/**
 * 趋势参考图服务接口。
 *
 * <p>为 TrendAgent 提供当前热门款式的参考图片列表，优先级：
 * <ol>
 *   <li>配置了 {@code deepay.trend.api-url} → 调用外部趋势 API</li>
 *   <li>未配置外部 API → 查询系统内近 7 天销量最高的款式图片（内部热销兜底）</li>
 *   <li>仍无数据 → 返回内置 fallback 图片</li>
 * </ol>
 * </p>
 */
public interface TrendService {

    /**
     * 获取与 keyword 相关的趋势参考图片列表。
     *
     * @param keyword 商品关键词（品类），可为 null
     * @return 参考图片 URL 列表（至少 1 张，正常情况 3～5 张）
     */
    List<String> getReferenceImages(String keyword);

    /**
     * 获取指定品类+风格的趋势热度分（0~100）。无数据时返回 50（中性分）。
     *
     * @param category 品类（如 外套 / 连衣裙）
     * @param style    风格标签（如 极简 / 工装）
     * @return 趋势热度分 0~100
     */
    double getTrendScore(String category, String style);

}
