package cn.iocoder.yudao.adminserver.modules.pay.service.order.impl;

import cn.iocoder.yudao.adminserver.modules.pay.controller.order.vo.PayOrderExportReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.controller.order.vo.PayOrderPageReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.dal.mysql.order.PayOrderMapper;
import cn.iocoder.yudao.adminserver.modules.pay.service.order.PayOrderService;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * 支付订单 Service 实现类
 *
 * @author aquan
 */
@Service
@Validated
public class PayOrderServiceImpl implements PayOrderService {

    @Resource
    private PayOrderMapper orderMapper;

    @Override
    public PayOrderDO getOrder(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    public List<PayOrderDO> getOrderList(Collection<Long> ids) {
        return orderMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<PayOrderDO> getOrderPage(PayOrderPageReqVO pageReqVO) {
        return orderMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PayOrderDO> getOrderList(PayOrderExportReqVO exportReqVO) {
        return orderMapper.selectList(exportReqVO);
    }

}
