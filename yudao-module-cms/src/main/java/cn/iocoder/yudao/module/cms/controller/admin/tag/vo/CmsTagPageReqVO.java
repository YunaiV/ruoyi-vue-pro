package cn.iocoder.yudao.module.cms.controller.admin.tag.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "Admin - CMS Tag Page Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CmsTagPageReqVO extends PageParam {

    @Schema(description = "Tag Name", example = "Java")
    private String name; // For filtering by name

    @Schema(description = "Tag Slug", example = "java")
    private String slug; // For filtering by slug
}
