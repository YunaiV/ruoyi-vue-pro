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
     * 根据用户输入生成候选图片 URL 列表（默认 3 张）。
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
     * 根据用户输入生成指定数量的候选图片 URL 列表。
     *
     * <p>与 {@link #generateImages(String)} 行为一致，额外支持指定生成张数（n）。
     * n 超出实现支持范围时，实现层应自动裁剪到合理区间（如 1～8 张）。</p>
     *
     * @param userPrompt 用户输入的一句话需求或完整 Prompt
     * @param n          期望生成的图片张数
     * @return 图片 URL 列表（至少 1 张）
     */
    List<String> generateImages(String userPrompt, int n);

}
