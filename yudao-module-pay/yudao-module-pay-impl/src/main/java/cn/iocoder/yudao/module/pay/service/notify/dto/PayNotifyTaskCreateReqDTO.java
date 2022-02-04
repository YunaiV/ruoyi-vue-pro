package cn.iocoder.yudao.module.pay.service.notify.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 支付通知创建 DTO
 *
 * @author 芋道源码
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayNotifyTaskCreateReqDTO {

    /**
     * 类型
     */
    @NotNull(message = "类型不能为空")
    private Integer type;
    /**
     * 数据编号
     */
    @NotNull(message = "数据编号不能为空")
    private Long dataId;

}
