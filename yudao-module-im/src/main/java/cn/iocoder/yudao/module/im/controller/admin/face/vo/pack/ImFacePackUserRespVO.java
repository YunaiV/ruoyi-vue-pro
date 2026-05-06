package cn.iocoder.yudao.module.im.controller.admin.face.vo.pack;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "IM 表情包（用户端） Response VO")
@Data
public class ImFacePackUserRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "表情包名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "猫主子")
    private String name;

    @Schema(description = "表情包图标", example = "https://cdn.example.com/face/pack/cat.png")
    private String icon;

    @Schema(description = "表情列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Item> items;

    @Schema(description = "IM 表情包项（用户端）")
    @Data
    public static class Item {

        @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
        private Long id;

        @Schema(description = "表情图 URL", requiredMode = Schema.RequiredMode.REQUIRED,
                example = "https://cdn.example.com/face/pack/cat-001.png")
        private String url;

        @Schema(description = "表情名", example = "狗头")
        private String name;

        @Schema(description = "渲染宽度（像素）", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
        private Integer width;

        @Schema(description = "渲染高度（像素）", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
        private Integer height;

    }

}
