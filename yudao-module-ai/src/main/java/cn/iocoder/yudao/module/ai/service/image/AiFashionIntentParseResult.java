package cn.iocoder.yudao.module.ai.service.image;

import cn.iocoder.yudao.module.ai.enums.image.AiFashionIntentEnum;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * AI 服装设计 NLP 意图解析结果值对象
 *
 * @author deepay
 */
@Getter
@Builder
public final class AiFashionIntentParseResult {

    /** 解析出的意图类型 */
    private final AiFashionIntentEnum intent;

    /** 本次请求生成变体数量（1=单款，>1=批量）*/
    private final int batchCount;

    /** 单款完整 SD Prompt（已增强）*/
    private final String basePrompt;

    /** 批量生成时各款的 Prompt 列表（长度等于 batchCount）*/
    private final List<String> variantPrompts;

    /** 解析出的颜色 SD 关键词，如 "red, scarlet" */
    private final String detectedColor;

    /** 解析出的颜色 Hex 值，如 "#FF0000"（供前端显示）*/
    private final String detectedColorHex;

    /** 解析出的风格 SD 关键词 */
    private final String detectedStyle;

    /** 解析出的面料 SD 关键词 */
    private final String detectedFabric;

    /** 解析出的长度 SD 关键词 */
    private final String detectedLength;

    /** 解析出的版型 SD 关键词 */
    private final String detectedFit;

    /** 解析出的服装品类 SD 关键词 */
    private final String detectedGarmentType;

    /** 用户可读的解析说明，如"已理解：生成5款甜酷风红色短款连衣裙"*/
    private final String interpretation;

    /** 修改项列表（MODIFY 类意图时填充），如 ["颜色→红色", "面料→牛仔"]*/
    private final List<String> modifications;

    /** 建议工作流模式（BASIC/STANDARD/PROFESSIONAL）*/
    private final String workflowMode;

    /** 建议质量预设（FAST/MEDIUM/HIGH/ULTRA）*/
    private final String qualityPreset;

}
