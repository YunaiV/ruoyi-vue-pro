package cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 首公里库存查询请求 VO
 */
@Data
@Schema(description = "首公里库存查询请求 VO")
public class TmsFirstMileStockQueryReqVO {

    @Schema(description = "仓库编号")
    @NotNull(message = "仓库编号不能为空")
    private Long warehouseId;

    @Schema(description = "产品部门对应关系列表")
    @NotEmpty(message = "产品部门对应关系列表不能为空")
    @Valid
    private List<ProductDeptRelation> relations;

    @Data
    @Schema(description = "产品部门对应关系")
    public static class ProductDeptRelation {

        @Schema(description = "产品编号")
        @NotNull(message = "产品编号不能为空")
        private Long productId;

        @Schema(description = "部门编号")
        @NotNull(message = "部门编号不能为空")
        private Long deptId;
    }
} 