package cn.iocoder.yudao.module.srm.api.purchase.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class SrmPurchaseInSaveItemReqDTO {

    /**
     * 主键
     */
    @NotNull(message = "明细主键不能为空")
    private Long id;
    /**
     * 实际入库量
     */
    @NotNull(message = "明细实际入库量不能为空")
    private Integer actualQty;


    @NotNull(message = "明细上游ID不能为空")
    private Long upstreamId;
}
