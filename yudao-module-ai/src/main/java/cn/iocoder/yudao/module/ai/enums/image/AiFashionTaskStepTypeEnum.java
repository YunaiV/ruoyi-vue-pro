package cn.iocoder.yudao.module.ai.enums.image;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 服装设计流水线步骤类型枚举
 *
 * <p>每次设计任务由以下步骤按顺序组成：
 * <ol>
 *   <li>SDXL  - 使用 SDXL 文生图生成基础服装设计图</li>
 *   <li>POSE  - 使用 ControlNet (img2img) 进行姿势控制（可选）</li>
 *   <li>FABRIC- 使用 img2img 进行面料/材质转换（可选）</li>
 *   <li>UPSCALE - 使用 extras-single-image 进行超分辨率提升（可选）</li>
 *   <li>THREE_D - 预留：3D 转换步骤（未来扩展）</li>
 * </ol>
 * </p>
 *
 * @author deepay
 */
@AllArgsConstructor
@Getter
public enum AiFashionTaskStepTypeEnum {

    SDXL("SDXL", "SDXL 基础生成", 0),
    POSE("POSE", "ControlNet 姿势控制", 1),
    FABRIC("FABRIC", "面料/材质转换", 2),
    UPSCALE("UPSCALE", "超分辨率提升", 3),
    THREE_D("THREE_D", "3D 转换（预留）", 4);

    /**
     * 步骤类型标识
     */
    private final String type;
    /**
     * 步骤名称
     */
    private final String name;
    /**
     * 默认执行顺序（0-based）
     */
    private final int defaultOrder;

}
