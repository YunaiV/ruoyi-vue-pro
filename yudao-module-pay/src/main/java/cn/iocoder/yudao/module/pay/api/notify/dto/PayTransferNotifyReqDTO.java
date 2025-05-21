package cn.iocoder.yudao.module.pay.api.notify.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;

/**
 * 转账单的通知 Request DTO
 *
 * @author jason
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayTransferNotifyReqDTO {

    /**
     * 商户转账单号
     */
    @NotEmpty(message = "商户转账单号不能为空")
    private String merchantTransferId;

    /**
     * 转账订单编号
     */
    @NotNull(message = "转账订单编号不能为空")
    private Long payTransferId;

}
