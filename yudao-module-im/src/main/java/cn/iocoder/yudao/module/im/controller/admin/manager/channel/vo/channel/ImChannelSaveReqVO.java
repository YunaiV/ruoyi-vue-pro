package cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.channel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Schema(description = "管理后台 - IM 频道新增 / 修改 Request VO")
@Data
public class ImChannelSaveReqVO {

    @Schema(description = "编号（修改时必填）", example = "1024")
    private Long id;

    @Schema(description = "频道业务码；唯一", requiredMode = Schema.RequiredMode.REQUIRED, example = "system_notice")
    @NotBlank(message = "频道编码不能为空")
    @Size(max = 64, message = "频道编码长度不能超过 64")
    @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "频道编码只能由小写字母 / 数字 / 下划线组成，且必须以字母开头")
    private String code;

    @Schema(description = "频道名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "系统公告")
    @NotBlank(message = "频道名称不能为空")
    @Size(max = 64, message = "频道名称长度不能超过 64")
    private String name;

    @Schema(description = "频道头像", example = "https://cdn.example.com/channel/system_notice.png")
    @Size(max = 512, message = "头像长度不能超过 512")
    private String avatar;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status; // 参见 CommonStatusEnum 枚举类（0 启用 / 1 禁用）

}
