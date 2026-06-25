package cn.iocoder.yudao.module.im.controller.admin.face.vo.useritem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "IM 个人表情 Response VO")
@Data
public class ImFaceUserItemRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "4096")
    private Long id;

    @Schema(description = "表情图 URL", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "https://cdn.example.com/face/user/abc.gif")
    private String url;

    @Schema(description = "表情名", example = "狗头")
    private String name;

    @Schema(description = "渲染宽度（像素）", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
    private Integer width;

    @Schema(description = "渲染高度（像素）", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
    private Integer height;

}
