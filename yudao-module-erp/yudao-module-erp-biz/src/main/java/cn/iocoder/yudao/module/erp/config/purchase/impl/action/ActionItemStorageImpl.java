package cn.iocoder.yudao.module.erp.config.purchase.impl.action;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpStorageStatus;
import com.alibaba.cola.statemachine.Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class ActionItemStorageImpl implements Action<ErpStorageStatus, ErpEventEnum, ErpPurchaseRequestItemsDO> {
    @Autowired
    private ErpPurchaseRequestItemsMapper mapper;

    @Autowired
    private ErpPurchaseRequestMapper requestMapper;

    @Override
    @Transactional
    public void execute(ErpStorageStatus f, ErpStorageStatus t, ErpEventEnum erpEventEnum, ErpPurchaseRequestItemsDO requestItemsDO) {
        ErpPurchaseRequestItemsDO itemsDO = mapper.selectById(requestItemsDO.getId());
        itemsDO.setOrderStatus(f.getCode());
        mapper.updateById(itemsDO);
        //日志记录状态变化 log.info
        log.info("采购订单{}状态变化：{}->{}", JSONUtil.toJsonStr(requestItemsDO), f, t);
    }
}
