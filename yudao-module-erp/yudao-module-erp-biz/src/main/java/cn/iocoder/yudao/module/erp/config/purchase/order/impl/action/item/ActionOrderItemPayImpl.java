package cn.iocoder.yudao.module.erp.config.purchase.order.impl.action.item;

import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpPaymentStatus;
import com.alibaba.cola.statemachine.Action;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ActionOrderItemPayImpl implements Action<ErpPaymentStatus, ErpEventEnum, ErpPurchaseOrderItemDO> {
    @Resource
    private ErpPurchaseOrderItemMapper itemMapper;

    @Override
    public void execute(ErpPaymentStatus f, ErpPaymentStatus t, ErpEventEnum erpEventEnum,
                        ErpPurchaseOrderItemDO context) {
        // 支付状态机执行
        // 1. 校验参数
        if (context == null) {
            return;
        }

        // 2. 更新支付状态
        context.setPayStatus(t.getCode());
        itemMapper.updateById(context);
        // 3. 记录日志
        log.info("子项支付状态机触发({})事件：对象ID={}，状态 {} -> {}",
            erpEventEnum.getDesc(),
            context.getId(),
            f.getDesc(),
            t.getDesc());
    }
}
