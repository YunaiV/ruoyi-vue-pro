package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

//审核状态请求类
@Data
public class ErpPurchaseRequestAuditStatusReq {
    // 请求ID
    @NotNull(message = "请求ID不能为空")
    private Long requestId;

    // 项目列表
    @NotNull(message = "项目列表不能为空")
    private List<requestItems> items;

    // 审核状态
    @NotNull(message = "审核状态不能为空")
    private Boolean reviewed;

    // 内部静态类，用于表示项目
    @Data
    public static class requestItems {
        // 项目ID
        @NotNull(message = "项目ID不能为空")
        private Long id;
        //批准数量
        @NotNull(message = "批准数量不能为空")
        private Integer approveCount;
    }
}
