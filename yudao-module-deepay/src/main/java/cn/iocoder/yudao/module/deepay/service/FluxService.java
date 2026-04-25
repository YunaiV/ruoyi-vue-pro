package cn.iocoder.yudao.module.deepay.service;

import java.util.List;

/**
 * AI 出图服务接口（FLUX / 其他文生图 API）。
 *
 * <p>封装与外部 AI 图片生成 API 的交互，为 DesignAgent 提供统一的出图能力。
 * 实现层负责：组装 prompt、调用远端 HTTP 接口、处理失败并返回保底图片。</p>
 */
public interface FluxService {

    /**
     * 根据用户输入生成候选图片 URL 列表。
     *
     * <p>实现要求：
     * <ul>
     *   <li>prompt 会在内部拼接固定模板前缀，调用方只需传入用户的一句话需求。</li>
     *   <li>正常情况下返回 2～3 张图片 URL。</li>
     *   <li>若 AI 服务失败，必须返回保底默认图片，不得抛出异常。</li>
     * </ul>
     * </p>
     *
     * @param userPrompt 用户输入的一句话需求，例如"极简羊绒大衣"
     * @return 图片 URL 列表（至少 1 张，正常情况 2～3 张）
     */
    List<String> generateImages(String userPrompt);

    /**
     * 生成指定数量的候选图片（Phase 8）。
     *
     * <p>默认实现：连续调用 {@link #generateImages(String)} 并合并去重结果，
     * 直至收集到 {@code count} 张或重试 3 次为止。
     * 有能力返回多张的实现类应覆盖此方法以减少 HTTP 调用次数。</p>
     *
     * @param userPrompt 用户输入
     * @param count      期望生成张数（建议 3~6）
     * @return 图片 URL 列表（数量 ≤ count，至少 1 张）
     */
    default List<String> generateImages(String userPrompt, int count) {
        java.util.Set<String> urls = new java.util.LinkedHashSet<>();
        int attempts = 0;
        while (urls.size() < count && attempts < 3) {
            List<String> batch = generateImages(userPrompt);
            if (batch != null) {
                urls.addAll(batch);
            }
            attempts++;
        }
        return new java.util.ArrayList<>(urls);
    }

}
