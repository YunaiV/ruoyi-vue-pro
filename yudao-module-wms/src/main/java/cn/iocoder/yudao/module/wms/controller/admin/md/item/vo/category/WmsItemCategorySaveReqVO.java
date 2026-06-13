package cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.category;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - WMS 商品分类新增/修改 Request VO")
@Data
public class WmsItemCategorySaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "父级编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "父级编号不能为空")
    private Long parentId;

    @Schema(description = "分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "C00000001")
    @NotEmpty(message = "分类编号不能为空")
    @Size(max = 20, message = "分类编号长度不能超过 20 个字符")
    private String code;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "原料")
    @NotEmpty(message = "分类名称不能为空")
    @Size(max = 30, message = "分类名称长度不能超过 30 个字符")
    private String name;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(value = CommonStatusEnum.class, message = "状态必须是 {value}")
    private Integer status;

}
