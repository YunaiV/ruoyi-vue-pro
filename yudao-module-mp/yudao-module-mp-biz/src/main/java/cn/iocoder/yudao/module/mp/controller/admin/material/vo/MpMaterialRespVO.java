package cn.iocoder.yudao.module.mp.controller.admin.material.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 公众号素材 Response VO")
@Data
public class MpMaterialRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "公众号账号的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long accountId;
    @Schema(description = "公众号账号的 appId", requiredMode = Schema.RequiredMode.REQUIRED, example = "wx1234567890")
    private String appId;

    @Schema(description = "素材的 media_id", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private String mediaId;

    @Schema(description = "文件类型 参见 WxConsts.MediaFileType 枚举", requiredMode = Schema.RequiredMode.REQUIRED, example = "image")
    private String type;

    @Schema(description = "是否永久 true - 永久；false - 临时", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean permanent;

    @Schema(description = "素材的 URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/1.png")
    private String url;


    @Schema(description = "名字", example = "yunai.png")
    private String name;

    @Schema(description = "公众号文件 URL 只有【永久素材】使用", example = "https://mmbiz.qpic.cn/xxx.mp3")
    private String mpUrl;

    @Schema(description = "视频素材的标题 只有【永久素材】使用", example = "我是标题")
    private String title;
    @Schema(description = "视频素材的描述 只有【永久素材】使用", example = "我是介绍")
    private String introduction;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
