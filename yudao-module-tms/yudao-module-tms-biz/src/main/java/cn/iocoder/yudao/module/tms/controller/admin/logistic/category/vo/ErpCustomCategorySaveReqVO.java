package cn.iocoder.yudao.module.tms.controller.admin.logistic.category.vo;

import cn.iocoder.yudao.module.system.api.utils.Validation;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.item.vo.ErpCustomCategoryItemSaveReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 海关分类新增/修改 Request VO")
@Data
public class ErpCustomCategorySaveReqVO {

    @Schema(description = "编号")
    @Null(groups = Validation.OnCreate.class, message = "创建时，海关分类id必须为空")
    @NotNull(groups = Validation.OnUpdate.class, message = "更新时，海关分类id不能为空")
    private Long id;

    @Schema(description = "材质-字典")
    @NotNull(message = "品类的材质不能为空")
    private Integer material;

    @Schema(description = "报关品名")
    @NotBlank(message = "报关品名不能为空")
    private String declaredType;

    @Schema(description = "英文品名")
    @NotBlank(message = "英文品名不能为空")
    private String declaredTypeEn;

    @Schema(description = "海关分类子表列表")
    @NotNull( message = "至少维护一个国别详情")
    @Valid
    private List<ErpCustomCategoryItemSaveReqVO> customRuleCategoryItems;

}