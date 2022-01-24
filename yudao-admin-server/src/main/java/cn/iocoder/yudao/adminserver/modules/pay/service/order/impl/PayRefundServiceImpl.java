package cn.iocoder.yudao.adminserver.modules.pay.service.order.impl;

import cn.iocoder.yudao.adminserver.modules.pay.controller.order.vo.refund.vo.PayRefundExportReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.controller.order.vo.refund.vo.PayRefundPageReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.dal.mysql.order.PayRefundMapper;
import cn.iocoder.yudao.adminserver.modules.pay.service.order.PayRefundService;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayRefundDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

/**
 * 退款订单 Service 实现类
 *
 * @author aquan
 */
@Service
@Validated
public class PayRefundServiceImpl implements PayRefundService {

    @Resource
    private PayRefundMapper refundMapper;

    @Override
    public PayRefundDO getRefund(Long id) {
        return refundMapper.selectById(id);
    }

    @Override
    public PageResult<PayRefundDO> getRefundPage(PayRefundPageReqVO pageReqVO) {
        return refundMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PayRefundDO> getRefundList(PayRefundExportReqVO exportReqVO) {
        return refundMapper.selectList(exportReqVO);
    }

}
