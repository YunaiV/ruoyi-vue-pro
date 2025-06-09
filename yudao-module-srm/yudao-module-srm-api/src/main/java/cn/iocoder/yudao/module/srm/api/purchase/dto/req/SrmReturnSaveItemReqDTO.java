package cn.iocoder.yudao.module.srm.api.purchase.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class SrmReturnSaveItemReqDTO {

    @NotNull(message = "明细ID不能为空")
    private Long id;

    @NotNull(message = "产品ID不能为空")
    private Long productId;
    /**
     * 实际出库量
     */
    @NotNull(message = "明细实际出库量不能为空")
    private Integer actualQty;

    /**
     * 来源明细行ID
     */
    @NotNull(message = "明细来源详情ID不能为空")
    private Long upstreamId;
}
