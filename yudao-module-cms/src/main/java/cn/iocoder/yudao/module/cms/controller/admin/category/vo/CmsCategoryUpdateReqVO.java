package cn.iocoder.yudao.module.cms.controller.admin.category.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import javax.validation.constraints.NotNull;

@Schema(description = "Admin - Update CMS Category Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CmsCategoryUpdateReqVO extends CmsCategoryBaseVO {

    @Schema(description = "Category ID", required = true, example = "1")
    @NotNull(message = "Category ID cannot be empty")
    private Long id;
}
