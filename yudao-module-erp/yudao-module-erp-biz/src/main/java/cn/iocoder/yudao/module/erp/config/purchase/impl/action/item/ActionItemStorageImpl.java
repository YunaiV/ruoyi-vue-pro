package cn.iocoder.yudao.module.erp.config.purchase.impl.action.item;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
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
    public void execute(ErpStorageStatus f, ErpStorageStatus t, ErpEventEnum erpEventEnum, ErpPurchaseRequestItemsDO context) {
        ErpPurchaseRequestItemsDO itemsDO = mapper.selectById(context.getId());
        itemsDO.setOrderStatus(f.getCode());
        ThrowUtil.ifSqlThrow(mapper.updateById(itemsDO), GlobalErrorCodeConstants.DB_BATCH_UPDATE_ERROR);
        //日志记录状态变化 log.info
        log.info("item入库状态机触发({})事件：将对象{},由状态 {}->{}", erpEventEnum.getDesc(), JSONUtil.toJsonStr(context), f.getDesc(), t.getDesc());
    }
}
