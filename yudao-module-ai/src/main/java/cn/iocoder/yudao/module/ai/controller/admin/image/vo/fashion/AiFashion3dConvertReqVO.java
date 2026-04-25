package cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 管理后台 - AI 服装 3D 转换 Request VO
 *
 * @author deepay
 */
@Schema(description = "管理后台 - AI 服装 3D 转换 Request VO")
@Data
public class AiFashion3dConvertReqVO {

    @Schema(description = "关联的设计任务ID（优先从任务获取图片）", example = "1024")
    private Long taskId;

    @Schema(description = "直接指定图片URL（taskId 为空时必填）", example = "https://example.com/fashion.png")
    private String imageUrl;

    @Schema(description = "颜色修改 Hex 值，如 #FF0000，null 表示不修改", example = "#FF0000")
    private String colorChange;

    @Schema(description = "需要生成的旋转角度列表", example = "[0, 45, 90, 135, 180, 225, 270, 315]")
    private List<Integer> rotationAngles = List.of(0, 45, 90, 135, 180, 225, 270, 315);

    @Schema(description = "网格精度，值越大越细腻但越慢（32/64/128）", example = "64")
    private Integer gridResolution = 64;

    @Schema(description = "输出格式 OBJ/GLTF/STL", example = "OBJ")
    private String outputFormat = "OBJ";

    @Schema(description = "是否生成旋转 GIF 动画（实际输出为多角度合成图）", example = "true")
    private Boolean generateGif = Boolean.TRUE;

    @Schema(description = "是否通过 img2img 生成多角度照片渲染", example = "true")
    private Boolean generateMultiAngle = Boolean.TRUE;

    @Schema(description = "原始设计提示词（用于多角度渲染）", example = "red dress, fashion photography")
    private String prompt;

}
