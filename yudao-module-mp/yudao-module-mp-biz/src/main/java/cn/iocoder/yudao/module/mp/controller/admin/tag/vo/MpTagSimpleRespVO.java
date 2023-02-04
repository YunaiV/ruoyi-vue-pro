package cn.iocoder.yudao.module.mp.controller.admin.tag.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 公众号标签精简信息 Response VO")
@Data
public class MpTagSimpleRespVO {

    @Schema(description = "编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "公众号的标签编号", required = true, example = "2048")
    private Long tagId;

    @Schema(description = "标签名称", required = true, example = "快乐")
    private String name;

}
