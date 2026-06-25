package cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.item;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - IM 表情包项新增 / 修改 Request VO")
@Data
public class ImFacePackItemSaveReqVO {

    @Schema(description = "编号（修改时必填）", example = "2048")
    private Long id;

    @Schema(description = "所属表情包编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "表情包编号不能为空")
    private Long packId;

    @Schema(description = "表情图 URL", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "https://cdn.example.com/face/pack/cat-001.png")
    @NotBlank(message = "表情图 URL 不能为空")
    @Size(max = 512, message = "表情图 URL 长度不能超过 512")
    private String url;

    @Schema(description = "表情名", example = "狗头")
    @Size(max = 64, message = "表情名长度不能超过 64")
    private String name;

    @Schema(description = "渲染宽度（像素）", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
    @NotNull(message = "渲染宽度不能为空")
    @Min(value = 1, message = "渲染宽度不能小于 1 像素")
    @Max(value = 2048, message = "渲染宽度不能大于 2048 像素")
    private Integer width;

    @Schema(description = "渲染高度（像素）", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
    @NotNull(message = "渲染高度不能为空")
    @Min(value = 1, message = "渲染高度不能小于 1 像素")
    @Max(value = 2048, message = "渲染高度不能大于 2048 像素")
    private Integer height;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(value = CommonStatusEnum.class, message = "状态必须是 {value}")
    private Integer status; // 参见 CommonStatusEnum 枚举类（0 启用 / 1 禁用）

}
