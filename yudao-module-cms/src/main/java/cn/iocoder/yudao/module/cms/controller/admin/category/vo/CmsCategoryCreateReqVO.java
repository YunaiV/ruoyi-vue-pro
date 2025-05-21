package cn.iocoder.yudao.module.cms.controller.admin.category.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "Admin - Create CMS Category Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CmsCategoryCreateReqVO extends CmsCategoryBaseVO {
    // Inherits name, slug, parentId, description from CmsCategoryBaseVO
    // No additional fields needed for creation beyond the base ones currently.
}
