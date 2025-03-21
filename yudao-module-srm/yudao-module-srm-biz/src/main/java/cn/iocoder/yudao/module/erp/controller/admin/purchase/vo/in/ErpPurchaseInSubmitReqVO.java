package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Collection;

@Data
public class ErpPurchaseInSubmitReqVO {
    @NotNull
    private Collection<Long> inIds;
}
