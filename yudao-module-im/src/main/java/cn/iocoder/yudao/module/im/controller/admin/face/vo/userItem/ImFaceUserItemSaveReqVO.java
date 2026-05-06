package cn.iocoder.yudao.module.im.controller.admin.face.vo.useritem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "IM 个人表情新增 Request VO")
@Data
public class ImFaceUserItemSaveReqVO {

    @Schema(description = "表情图 URL", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "https://cdn.example.com/face/user/abc.gif")
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

}
