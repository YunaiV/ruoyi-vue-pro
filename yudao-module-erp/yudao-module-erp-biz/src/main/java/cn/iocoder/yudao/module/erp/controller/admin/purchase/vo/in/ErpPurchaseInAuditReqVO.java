package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in;

import cn.iocoder.yudao.module.erp.controller.admin.tools.validation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

public class ErpPurchaseInAuditReqVO {
    @NotNull(groups = {validation.OnSubmitAudit.class, validation.OnAudit.class}, message = "入库单ID不能为空")
    @Size(min = 1, groups = validation.OnAudit.class, message = "入库单ID审核时候只能传一个")
    @Schema(description = "入库单ID集合")
    private List<Long> inIds;

    // 入库单列表
    @NotNull(groups = {validation.OnSwitch.class}, message = "入库单列表不能为空")
    @Schema(description = "入库单列表")
    private List<@Valid item> items;

    // 审核/反审核
    @NotNull(groups = validation.OnAudit.class, message = "审核状态不能为空")
    @Schema(description = "审核/反审核")
    private Boolean reviewed;
    //通过与否
    @NotNull(groups = validation.OnAudit.class, message = "审核通过与否不能为空")
    @Schema(description = "审核通过/不通过")
    private Boolean pass;

    //审核意见
    @Schema(description = "审核意见")
    private String reviewComment;

    //开关
    @Schema(description = "启用/关闭")
    @NotNull(groups = validation.OnSwitch.class, message = "开关不能为空")
    private Boolean enable;

    @Data
    public static class item {
        // 入库单ID
        @NotNull(message = "采购项ID不能为空")
        @Schema(description = "采购项ID")
        private Long id;
//        //批准数量
//        @NotNull(groups = validation.OnAudit.class,message = "批准数量不能为空")
//        @Schema(description = "批准数量")
//        private Integer approveCount;
    }
}
