package cn.iocoder.yudao.module.srm.config.machine.request;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SrmRequestInMachineContext {

    //入库差额
    private BigDecimal inCount;

    //申请项->申请单下的子项
    private Long applyItemId;
}
