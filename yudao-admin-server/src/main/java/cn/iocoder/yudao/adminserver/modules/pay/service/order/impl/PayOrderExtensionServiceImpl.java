package cn.iocoder.yudao.adminserver.modules.pay.service.order.impl;

import cn.iocoder.yudao.adminserver.modules.pay.dal.mysql.order.PayOrderExtensionMapper;
import cn.iocoder.yudao.adminserver.modules.pay.service.order.PayOrderExtensionService;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayOrderExtensionDO;
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
public class PayOrderExtensionServiceImpl implements PayOrderExtensionService {

    @Resource
    private PayOrderExtensionMapper orderExtensionMapper;

    @Override
    public PayOrderExtensionDO getOrderExtension(Long id) {
        return orderExtensionMapper.selectById(id);
    }

    @Override
    public List<PayOrderExtensionDO> getOrderExtensionList(Collection<Long> ids) {
        return orderExtensionMapper.selectBatchIds(ids);
    }

}
