package cn.iocoder.yudao.module.crm.controller.admin.category.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 商品分类分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductCategoryPageReqVO extends PageParam {

    @Schema(description = "分类名称（模糊查询）", example = "电子")
    private String name;

    @Schema(description = "状态：0-启用 1-禁用", example = "0")
    private Integer status;

    @Schema(description = "父分类ID", example = "0")
    private Long parentId;

}
