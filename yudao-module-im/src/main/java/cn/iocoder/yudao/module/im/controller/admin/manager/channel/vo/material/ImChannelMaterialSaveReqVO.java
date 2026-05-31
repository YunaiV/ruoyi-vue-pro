package cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.material;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Schema(description = "管理后台 - IM 频道素材新增 / 修改 Request VO")
@Data
public class ImChannelMaterialSaveReqVO {

    @Schema(description = "编号（修改时必填）", example = "1024")
    private Long id;

    @Schema(description = "频道编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "频道编号不能为空")
    private Long channelId;

    @Schema(description = "内容类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "内容类型不能为空")
    private Integer type; // 参见 ImChannelMaterialTypeEnum 枚举类

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "双十一活动来啦")
    @NotBlank(message = "标题不能为空")
    @Size(max = 128, message = "标题长度不能超过 128")
    private String title;

    @Schema(description = "封面图", example = "https://cdn.example.com/cover.png")
    @Size(max = 512, message = "封面图长度不能超过 512")
    private String coverUrl;

    @Schema(description = "摘要", example = "全场五折，戳详情看玩法")
    @Size(max = 255, message = "摘要长度不能超过 255")
    private String summary;

    @Schema(description = "正文；富文本 HTML")
    private String content;

    @Schema(description = "跳转链接；为空表示走客户端内置详情页", example = "https://example.com/activity/123")
    @Size(max = 512, message = "跳转链接长度不能超过 512")
    private String url;

}
