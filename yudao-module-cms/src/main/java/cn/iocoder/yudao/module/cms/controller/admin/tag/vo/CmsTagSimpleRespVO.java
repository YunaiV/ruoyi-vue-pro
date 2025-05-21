package cn.iocoder.yudao.module.cms.controller.admin.tag.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Admin - CMS Tag Simple Response VO")
@Data
public class CmsTagSimpleRespVO {

    @Schema(description = "Tag ID", required = true, example = "1")
    private Long id;

    @Schema(description = "Tag Name", required = true, example = "Java")
    private String name;
}
