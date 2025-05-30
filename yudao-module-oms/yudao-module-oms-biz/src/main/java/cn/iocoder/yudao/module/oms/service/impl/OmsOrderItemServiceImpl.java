package cn.iocoder.yudao.module.oms.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oms.controller.admin.order.item.vo.OmsOrderItemPageReqVO;
import cn.iocoder.yudao.module.oms.controller.admin.order.item.vo.OmsOrderItemSaveReqVO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsOrderItemDO;
import cn.iocoder.yudao.module.oms.dal.mysql.OmsOrderItemMapper;
import cn.iocoder.yudao.module.oms.service.OmsOrderItemService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_ORDER_ITEM_NOT_EXISTS;
import static com.baomidou.mybatisplus.extension.toolkit.Db.saveBatch;

/**
 * OMS订单项 Service 实现类
 *
 * @author 谷毛毛
 */
@Service
@Validated
@Slf4j
public class OmsOrderItemServiceImpl implements OmsOrderItemService {

    @Resource
    private OmsOrderItemMapper orderItemMapper;


    @Override
    public Long createOrderItem(OmsOrderItemSaveReqVO createReqVO) {
        // 插入
        OmsOrderItemDO orderItem = BeanUtils.toBean(createReqVO, OmsOrderItemDO.class);
        orderItemMapper.insert(orderItem);
        // 返回
        return orderItem.getId();
    }

    @Override
    public void updateOrderItem(OmsOrderItemSaveReqVO updateReqVO) {
        // 校验存在
        validateOrderItemExists(updateReqVO.getId());
        // 更新
        OmsOrderItemDO updateObj = BeanUtils.toBean(updateReqVO, OmsOrderItemDO.class);
        orderItemMapper.updateById(updateObj);
    }

    @Override
    public void deleteOrderItem(Long id) {
        // 校验存在
        validateOrderItemExists(id);
        // 删除
        orderItemMapper.deleteById(id);
    }

    private void validateOrderItemExists(Long id) {
        if (orderItemMapper.selectById(id) == null) {
            throw exception(OMS_ORDER_ITEM_NOT_EXISTS);
        }
    }

    @Override
    public OmsOrderItemDO getOrderItem(Long id) {
        return orderItemMapper.selectById(id);
    }

    @Override
    public PageResult<OmsOrderItemDO> getOrderItemPage(OmsOrderItemPageReqVO pageReqVO) {
        return orderItemMapper.selectPage(pageReqVO);
    }

    @Override
    public void saveOmsOrderItemDOList(List<OmsOrderItemDO> omsOrderItemDOList) {
        saveBatch(omsOrderItemDOList);
    }

    @Override
    public int deleteByOrderIds(List<Long> orderIds) {
        return orderItemMapper.deleteByOrderIds(orderIds);
    }

    @Override
    public OmsOrderItemDO getOrderItemByExternalId(String externalId) {
        return orderItemMapper.selectOne(OmsOrderItemDO::getExternalId, externalId);
    }

    @Override
    public void updateOrderItems(List<OmsOrderItemDO> updateOrderItems) {
        orderItemMapper.updateById(updateOrderItems);
    }


}