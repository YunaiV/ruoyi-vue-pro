package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Collection;

@Data
public class SrmPurchaseInSubmitReqVO {
    @NotNull(message = "采购到货单ID不能为空")
    private Collection<Long> arriveIds;
}
