package cn.iocoder.yudao.module.srm.config.machine.outItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SrmPurchaseOutMachineContext {

    // 出库id
    private Long returnId;
}
