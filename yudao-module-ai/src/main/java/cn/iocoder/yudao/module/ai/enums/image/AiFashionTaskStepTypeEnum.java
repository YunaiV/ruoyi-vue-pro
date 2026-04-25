package cn.iocoder.yudao.module.ai.enums.image;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 服装设计流水线步骤类型枚举
 *
 * <p>对应 Python 参考代码中的 {@code WorkflowStage} 枚举，共 8 个阶段。</p>
 *
 * <pre>
 * ┌────────┬────────────────────────────┬──────────────┬──────────────────────────────────┐
 * │ 阶段   │ 说明                       │ SD 端点      │ 触发条件                         │
 * ├────────┼────────────────────────────┼──────────────┼──────────────────────────────────┤
 * │CONCEPT │ 快速概念草图               │ txt2img      │ 始终（PROFESSIONAL/FULL）         │
 * │SKETCH  │ 清晰线稿/平面款式图        │ txt2img/img2img│ 始终                            │
 * │DETAIL  │ 细节强化（含 ControlNet）  │ img2img      │ PROFESSIONAL/FULL                │
 * │TEXTURE │ 面料/纹理转换              │ img2img      │ 有 fabricRefUrl 时               │
 * │POSE    │ 姿势控制（ControlNet）     │ img2img      │ 有 poseImageUrl 时               │
 * │RENDER  │ 最终高质渲染               │ img2img      │ PROFESSIONAL/FULL                │
 * │UPSAMPLE│ 超分辨率（等同 UPSCALE）   │ extras       │ upscale=true 时                  │
 * │THREE_D │ 3D 网格重建                │ 预留         │ require3d=true 时                │
 * └────────┴────────────────────────────┴──────────────┴──────────────────────────────────┘
 *
 * 向后兼容别名（v1）：SDXL→SKETCH，FABRIC→TEXTURE，UPSCALE→UPSAMPLE
 * </pre>
 *
 * @author deepay
 */
@AllArgsConstructor
@Getter
public enum AiFashionTaskStepTypeEnum {

    // ===== 8 阶段正式定义（对应 Python WorkflowStage）=====

    /** 概念草图：txt2img 低步数快速出概念 */
    CONCEPT("CONCEPT", "概念设计", 0),
    /** 线稿/款式图：txt2img 中步数出清晰草图 */
    SKETCH("SKETCH", "草图生成", 1),
    /** 细节强化：img2img + ControlNet 线稿/深度 */
    DETAIL("DETAIL", "细节设计", 2),
    /** 面料/纹理转换：img2img 应用面料参考 */
    TEXTURE("TEXTURE", "纹理应用", 3),
    /** 姿势控制：img2img + ControlNet openpose */
    POSE("POSE", "姿势控制", 4),
    /** 最终渲染：img2img 高步数精细渲染 */
    RENDER("RENDER", "最终渲染", 5),
    /** 超分辨率：extras-single-image */
    UPSAMPLE("UPSAMPLE", "超分辨率", 6),
    /** 3D 重建：预留，网格+纹理+UV 输出 */
    THREE_D("THREE_D", "3D 重建", 7),

    // ===== v1 向后兼容别名（不在新工作流中出现，仅用于旧任务） =====

    /** @deprecated 使用 {@link #SKETCH} */
    @Deprecated
    SDXL("SDXL", "SDXL 基础生成（已弃用，请用 SKETCH）", 1),
    /** @deprecated 使用 {@link #TEXTURE} */
    @Deprecated
    FABRIC("FABRIC", "面料转换（已弃用，请用 TEXTURE）", 3),
    /** @deprecated 使用 {@link #UPSAMPLE} */
    @Deprecated
    UPSCALE("UPSCALE", "超分（已弃用，请用 UPSAMPLE）", 6);

    /** 步骤类型标识 */
    private final String type;
    /** 步骤名称 */
    private final String name;
    /** 默认执行顺序（0-based） */
    private final int defaultOrder;

    /** 根据 type 字符串查找，找不到返回 null */
    public static AiFashionTaskStepTypeEnum of(String type) {
        if (type == null) {
            return null;
        }
        for (AiFashionTaskStepTypeEnum e : values()) {
            if (e.type.equalsIgnoreCase(type)) {
                return e;
            }
        }
        return null;
    }

}
