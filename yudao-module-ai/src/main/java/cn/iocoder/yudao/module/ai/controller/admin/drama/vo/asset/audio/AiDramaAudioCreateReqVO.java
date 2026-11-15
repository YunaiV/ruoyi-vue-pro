package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.audio;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaAudioTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 短剧音频上传 Request VO：角色样音、旁白")
@Data
public class AiDramaAudioCreateReqVO {

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "项目编号不能为空")
    private Long projectId;

    @Schema(description = "角色编号：角色样音必填，旁白为空", example = "1")
    private Long characterId;

    @Schema(description = "类型，参见 AiDramaAudioTypeEnum 枚举类；不能直接上传选用样音", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "类型不能为空")
    @InEnum(AiDramaAudioTypeEnum.class)
    private Integer type;

    @Schema(description = "文件地址：先通过文件上传接口上传", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/1.mp3")
    @NotBlank(message = "文件地址不能为空")
    @Size(max = 1024, message = "文件地址不能超过 1024 个字符")
    private String fileUrl;

}
