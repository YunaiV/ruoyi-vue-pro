package cn.iocoder.yudao.module.erp.api.purchase;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ErpInCountDTO {

    private Long orderItemId;
    //差额
    private BigDecimal count;
}
