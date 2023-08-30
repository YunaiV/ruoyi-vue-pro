package cn.iocoder.yudao.module.trade.controller.app.cart.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Collection;

@Schema(description = "用户 App - 购物车更新是否选中 Request VO")
@Data
public class AppCartUpdateSelectedReqVO {

    @Schema(description = "编号列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024,2048")
    @NotNull(message = "编号列表不能为空")
    private Collection<Long> ids;

    @Schema(description = "是否选中", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否选中不能为空")
    private Boolean selected;

}
