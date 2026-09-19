package cn.iocoder.yudao.module.oa.controller.admin.contact.vo.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Schema(description = "管理后台 - OA 联系人分类新增/修改 Request VO")
@Data
public class OaContactCategorySaveReqVO {

    @Schema(description = "分类编号", example = "1024")
    private Long id;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "客户")
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过 50 个字符")
    private String name;

    @Schema(description = "显示排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "显示排序不能为空")
    private Integer sort;

}
