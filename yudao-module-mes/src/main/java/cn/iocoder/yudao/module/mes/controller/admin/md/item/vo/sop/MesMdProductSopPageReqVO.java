package cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.sop;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 产品SOP分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesMdProductSopPageReqVO extends PageParam {

    @Schema(description = "物料产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "69")
    @NotNull(message = "物料产品ID不能为空")
    private Long itemId;

    @Schema(description = "标题", example = "步骤一")
    private String title;

}
