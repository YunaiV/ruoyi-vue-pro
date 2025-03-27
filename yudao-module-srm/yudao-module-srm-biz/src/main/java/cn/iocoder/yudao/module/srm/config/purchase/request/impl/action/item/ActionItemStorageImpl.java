package cn.iocoder.yudao.module.srm.config.purchase.request.impl.action.item;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.module.srm.api.purchase.SrmInCountDTO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.ErpPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.ErpStorageStatus;
import com.alibaba.cola.statemachine.Action;
import com.alibaba.cola.statemachine.StateMachine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.PURCHASE_REQUEST_STORAGE_STATE_MACHINE_NAME;

@Slf4j
@Component
public class ActionItemStorageImpl implements Action<ErpStorageStatus, SrmEventEnum, SrmInCountDTO> {
    @Autowired
    private ErpPurchaseRequestItemsMapper mapper;
    @Resource(name = PURCHASE_REQUEST_STORAGE_STATE_MACHINE_NAME)
    private StateMachine<ErpStorageStatus, SrmEventEnum, ErpPurchaseRequestDO> stateMachine;
    @Autowired
    private ErpPurchaseRequestMapper erpPurchaseRequestMapper;

    @Override
    @Transactional
    public void execute(ErpStorageStatus f, ErpStorageStatus t, SrmEventEnum event, SrmInCountDTO context) {
        ErpPurchaseRequestItemsDO itemsDO = mapper.selectById(context.getApplyItemId());
        if (event == SrmEventEnum.STOCK_ADJUSTMENT) {
            BigDecimal oldCount = itemsDO.getInCount() == null ? BigDecimal.ZERO : itemsDO.getInCount();
            BigDecimal changeCount = context.getInCount();
            itemsDO.setInCount(oldCount.add(changeCount));//入库数量
            //根据入库数量来动态计算当前状态

            if (itemsDO.getInCount().compareTo(BigDecimal.valueOf(itemsDO.getCount())) >= 0) {
                //入库量 >= 申请量
                t = ErpStorageStatus.ALL_IN_STORAGE;
            } else if (itemsDO.getInCount().compareTo(BigDecimal.valueOf(itemsDO.getCount())) < 0) {
                //入库量 < 申请量
                t = ErpStorageStatus.PARTIALLY_IN_STORAGE;
            } else {
                t = ErpStorageStatus.NONE_IN_STORAGE;
            }
        } else {
            itemsDO.setInStatus(t.getCode());//状态
        }
        ThrowUtil.ifSqlThrow(mapper.updateById(itemsDO), GlobalErrorCodeConstants.DB_BATCH_UPDATE_ERROR);
        //传递事件给主申请单
        ErpPurchaseRequestDO requestDO = erpPurchaseRequestMapper.selectById(itemsDO.getRequestId());
        stateMachine.fireEvent(ErpStorageStatus.fromCode(requestDO.getInStatus()), event, requestDO);
        log.debug("item入库状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), f.getDesc(), t.getDesc());
    }
}
