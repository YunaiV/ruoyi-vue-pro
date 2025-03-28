package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Collection;

@Data
public class SrmPurchaseInSubmitReqVO {
    @NotNull
    private Collection<Long> inIds;
}
