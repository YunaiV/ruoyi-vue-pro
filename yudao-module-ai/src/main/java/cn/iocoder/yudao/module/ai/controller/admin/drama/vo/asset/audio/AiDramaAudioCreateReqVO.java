package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.audio;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaAudioTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AI 短剧音频上传 Request VO：角色样音、旁白、配乐、参考音频")
@Data
public class AiDramaAudioCreateReqVO {

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "项目编号不能为空")
    private Long projectId;

    @Schema(description = "角色编号：角色样音必填，其它为空", example = "1")
    private Long characterId;

    @Schema(description = "类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "类型不能为空")
    @InEnum(AiDramaAudioTypeEnum.class)
    private Integer type;

    @Schema(description = "名称", example = "雨夜钢琴")
    @Size(max = 128, message = "名称不能超过 128 个字符")
    private String name;

    @Schema(description = "文件地址：先通过文件上传接口上传", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/1.mp3")
    @NotBlank(message = "文件地址不能为空")
    @Size(max = 1024, message = "文件地址不能超过 1024 个字符")
    private String fileUrl;

    @Schema(description = "时长（秒）", example = "12.5")
    private Double duration;

    @Schema(description = "使用的分镜编号：配乐、参考音频使用；为空表示候选", example = "[1, 2]")
    private List<Long> storyboardIds;

}
