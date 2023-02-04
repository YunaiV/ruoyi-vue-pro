package cn.iocoder.yudao.module.mp.controller.admin.material.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 公众号素材上传结果 Response VO")
@Data
public class MpMaterialUploadRespVO {

    @Schema(description = "素材的 media_id", required = true, example = "123")
    private String mediaId;

    @Schema(description = "素材的 URL", required = true, example = "https://www.iocoder.cn/1.png")
    private String url;

}
