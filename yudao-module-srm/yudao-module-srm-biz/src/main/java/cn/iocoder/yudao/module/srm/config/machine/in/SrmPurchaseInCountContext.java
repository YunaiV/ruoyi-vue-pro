package cn.iocoder.yudao.module.srm.config.machine.in;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SrmPurchaseInCountContext {

    //到货单ID
    private Long arriveId;

}
