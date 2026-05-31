package cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.pack;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Schema(description = "管理后台 - IM 表情包新增 / 修改 Request VO")
@Data
public class ImFacePackSaveReqVO {

    @Schema(description = "编号（修改时必填）", example = "1024")
    private Long id;

    @Schema(description = "表情包名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "猫主子")
    @NotBlank(message = "表情包名称不能为空")
    @Size(max = 64, message = "表情包名称长度不能超过 64")
    private String name;

    @Schema(description = "表情包图标", example = "https://cdn.example.com/face/pack/cat.png")
    @Size(max = 512, message = "图标长度不能超过 512")
    private String icon;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(value = CommonStatusEnum.class, message = "状态必须是 {value}")
    private Integer status; // 参见 CommonStatusEnum 枚举类（0 启用 / 1 禁用）

}
