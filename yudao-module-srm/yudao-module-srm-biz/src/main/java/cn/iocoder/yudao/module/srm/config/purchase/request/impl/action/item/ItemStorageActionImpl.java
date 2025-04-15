package cn.iocoder.yudao.module.srm.config.purchase.request.impl.action.item;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.module.srm.api.purchase.SrmInCountDTO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseRequestMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmStorageStatus;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.PURCHASE_REQUEST_STORAGE_STATE_MACHINE_NAME;

@Slf4j
@Component
public class ItemStorageActionImpl implements Action<SrmStorageStatus, SrmEventEnum, SrmInCountDTO> {
    @Autowired
    private SrmPurchaseRequestItemsMapper mapper;
    @Resource(name = PURCHASE_REQUEST_STORAGE_STATE_MACHINE_NAME)
    private StateMachine<SrmStorageStatus, SrmEventEnum, SrmPurchaseRequestDO> stateMachine;
    @Autowired
    private SrmPurchaseRequestMapper srmPurchaseRequestMapper;

    @Override
    @Transactional
    public void execute(SrmStorageStatus f, SrmStorageStatus t, SrmEventEnum event, SrmInCountDTO context) {
        SrmPurchaseRequestItemsDO itemsDO = mapper.selectById(context.getApplyItemId());
        if (event == SrmEventEnum.STOCK_ADJUSTMENT) {
            BigDecimal oldCount = itemsDO.getInboundClosedQty() == null ? BigDecimal.ZERO : itemsDO.getInboundClosedQty();
            BigDecimal changeCount = context.getInCount();
            itemsDO.setInboundClosedQty(oldCount.add(changeCount));//入库数量
            //根据入库数量来动态计算当前状态

            if (itemsDO.getInboundClosedQty().compareTo(BigDecimal.valueOf(itemsDO.getQty())) >= 0) {
                //入库量 >= 申请量
                t = SrmStorageStatus.ALL_IN_STORAGE;
            } else if (itemsDO.getInboundClosedQty().compareTo(BigDecimal.valueOf(itemsDO.getQty())) < 0) {
                //入库量 < 申请量
                t = SrmStorageStatus.PARTIALLY_IN_STORAGE;
            } else {
                t = SrmStorageStatus.NONE_IN_STORAGE;
            }
        } else {
            itemsDO.setInStatus(t.getCode());//状态
        }
        ThrowUtil.ifSqlThrow(mapper.updateById(itemsDO), GlobalErrorCodeConstants.DB_BATCH_UPDATE_ERROR);
        //传递事件给主申请单
        SrmPurchaseRequestDO requestDO = srmPurchaseRequestMapper.selectById(itemsDO.getRequestId());
        stateMachine.fireEvent(SrmStorageStatus.fromCode(requestDO.getInStatus()), event, requestDO);
        log.debug("item入库状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), f.getDesc(), t.getDesc());
    }
}
