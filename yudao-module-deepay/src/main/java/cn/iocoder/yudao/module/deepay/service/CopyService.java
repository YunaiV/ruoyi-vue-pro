package cn.iocoder.yudao.module.deepay.service;

/**
 * 商品文案生成服务接口。
 *
 * <p>调用 AI 文本 API（OpenAI-compatible chat completions）为商品生成标题和描述，
 * 未配置 API 时 fallback 到模板字符串。</p>
 */
public interface CopyService {

    /**
     * 生成商品标题。
     *
     * @param keyword 商品关键词，例如"极简羊绒大衣"
     * @return 商品标题（30 字以内）
     */
    String generateTitle(String keyword);

    /**
     * 生成商品卖点描述。
     *
     * @param keyword 商品关键词，例如"极简羊绒大衣"
     * @return 卖点描述（80 字以内）
     */
    String generateDescription(String keyword);

}
