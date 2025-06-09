package cn.iocoder.yudao.module.tms.service.first.mile.impl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.module.tms.api.first.mile.request.TmsFistMileRequestItemDTO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.TmsFirstMileRequestDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.item.TmsFirstMileRequestItemDO;
import cn.iocoder.yudao.module.tms.dal.mysql.first.mile.request.item.TmsFirstMileRequestItemMapper;
import cn.iocoder.yudao.module.tms.enums.TmsEventEnum;
import cn.iocoder.yudao.module.tms.enums.status.TmsOffStatus;
import cn.iocoder.yudao.module.tms.enums.status.TmsOrderStatus;
import cn.iocoder.yudao.module.tms.service.first.mile.request.TmsFirstMileRequestItemService;
import cn.iocoder.yudao.module.tms.service.first.mile.request.TmsFirstMileRequestService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.tms.enums.TmsErrorCodeConstants.FIRST_MILE_REQUEST_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.module.tms.enums.TmsStateMachines.*;

/**
 * 头程申请表明细 Service 实现类
 *
 * @author wdy
 */
@Service
@Validated
public class TmsFirstMileRequestItemServiceImpl implements TmsFirstMileRequestItemService {

    @Resource
    private TmsFirstMileRequestItemMapper firstMileRequestItemMapper;
    @Autowired
    @Lazy
    TmsFirstMileRequestService tmsFirstMileRequestService;
    @Resource(name = FIRST_MILE_REQUEST_ITEM_OFF_STATE_MACHINE)
    private StateMachine<TmsOffStatus, TmsEventEnum, TmsFirstMileRequestItemDO> offItemStatusMachine;
    @Resource(name = FIRST_MILE_REQUEST_ITEM_ORDER_STATE_MACHINE)
    private StateMachine<TmsOrderStatus, TmsEventEnum, TmsFistMileRequestItemDTO> orderItemStatusMachine;
    //
    @Resource(name = FIRST_MILE_REQUEST_OFF_STATE_MACHINE)
    private StateMachine<TmsOffStatus, TmsEventEnum, TmsFirstMileRequestDO> offStatusStatusMachine;
    @Resource(name = FIRST_MILE_REQUEST_PURCHASE_ORDER_STATE_MACHINE)
    private StateMachine<TmsOrderStatus, TmsEventEnum, TmsFirstMileRequestDO> orderStatusStatusMachine;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createFirstMileRequestItemList(Long requestId, List<TmsFirstMileRequestItemDO> list) {
        list.forEach(o -> o.setRequestId(requestId));
        firstMileRequestItemMapper.insertBatch(list);
        //初始化子表状态
        initSlaveStatus(list);
    }

    private void initSlaveStatus(List<TmsFirstMileRequestItemDO> list) {
        for (TmsFirstMileRequestItemDO item : list) {
            orderItemStatusMachine.fireEvent(TmsOrderStatus.OT_ORDERED, TmsEventEnum.ORDER_INIT, TmsFistMileRequestItemDTO.builder().itemId(item.getId()).build());
            offItemStatusMachine.fireEvent(TmsOffStatus.OPEN, TmsEventEnum.OFF_INIT, item);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFirstMileRequestItemList(Long requestId, List<TmsFirstMileRequestItemDO> list) {
        // 获取原有的子表数据
        List<TmsFirstMileRequestItemDO> oldList = firstMileRequestItemMapper.selectListByRequestId(requestId);

        List<List<TmsFirstMileRequestItemDO>> diffedList = diffList(oldList, list,
            (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

        if (CollUtil.isNotEmpty(diffedList.get(0))) {
            diffedList.get(0).forEach(item -> item.setRequestId(requestId));
            firstMileRequestItemMapper.insertBatch(diffedList.get(0));
            initSlaveStatus(diffedList.get(0));
        }
        if (CollUtil.isNotEmpty(diffedList.get(1))) {
            firstMileRequestItemMapper.updateBatch(diffedList.get(1));
        }
        if (CollUtil.isNotEmpty(diffedList.get(2))) {
            // 获取要删除的ID列表
            List<Long> deleteIds = convertList(diffedList.get(2), TmsFirstMileRequestItemDO::getId);
            // 删除数据
            firstMileRequestItemMapper.deleteByIds(deleteIds);
            // 获取主表数据
            TmsFirstMileRequestDO requestDO = tmsFirstMileRequestService.getFirstMileRequest(requestId);
            if (requestDO != null) {
                // 触发主表状态机事件
                offStatusStatusMachine.fireEvent(TmsOffStatus.fromCode(requestDO.getOffStatus()), TmsEventEnum.MANUAL_CLOSE, requestDO);
                orderStatusStatusMachine.fireEvent(TmsOrderStatus.fromCode(requestDO.getOrderStatus()), TmsEventEnum.ORDER_ADJUSTMENT, requestDO);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFirstMileRequestItem(Long id) {
        // 校验存在
        validateFirstMileRequestItemExists(id);
        // 删除
        firstMileRequestItemMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFirstMileRequestItemByRequestId(Long requestId) {
        firstMileRequestItemMapper.deleteByRequestId(requestId);
    }

    @Override
    public TmsFirstMileRequestItemDO validateFirstMileRequestItemExists(Long id) {
        TmsFirstMileRequestItemDO tmsFirstMileRequestItemDO = firstMileRequestItemMapper.selectById(id);
        if (tmsFirstMileRequestItemDO == null) {
            throw exception(FIRST_MILE_REQUEST_ITEM_NOT_EXISTS);
        }
        return tmsFirstMileRequestItemDO;
    }

    @Override
    public TmsFirstMileRequestItemDO getFirstMileRequestItem(Long id) {
        return firstMileRequestItemMapper.selectById(id);
    }

    /**
     * 通过关联id获得头程申请单
     *
     * @param id 关联申请项id
     * @return 头程申请单
     */
    @Override
    public List<TmsFirstMileRequestItemDO> getFirstMileRequestItemListByRequestId(Long id) {
        return firstMileRequestItemMapper.selectListByRequestId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFirstMileRequestItemStatus(Long id, Integer openStatus, Integer orderStatus, Integer closeQty) {
        TmsFirstMileRequestItemDO firstMileRequestItemDO = validateFirstMileRequestItemExists(id);
        if (openStatus != null) {
            firstMileRequestItemDO.setOffStatus(openStatus);
        }
        if (orderStatus != null) {
            firstMileRequestItemDO.setOrderStatus(orderStatus).setOrderClosedQty(closeQty);
        }
        firstMileRequestItemMapper.updateById(firstMileRequestItemDO);
    }
}