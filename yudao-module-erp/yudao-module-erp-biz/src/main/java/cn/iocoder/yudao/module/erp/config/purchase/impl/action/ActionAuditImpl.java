package cn.iocoder.yudao.module.erp.config.purchase.impl.action;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpOffStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpOrderStatus;
import com.alibaba.cola.statemachine.Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_REQUEST_PROCESS_FAIL_CLOSE;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_REQUEST_PROCESS_FAIL_ORDERED;


@Slf4j
@Component
public class ActionAuditImpl implements Action<ErpAuditStatus, ErpEventEnum, ErpPurchaseRequestDO> {
    @Autowired
    private ErpPurchaseRequestMapper mapper;

    @Override
    @Transactional
    public void execute(ErpAuditStatus from, ErpAuditStatus to, ErpEventEnum event, ErpPurchaseRequestDO context) {
        validate(from, to, event, context);
        //审核通过(创建采购订单+批准数量+制空未通过意见)
        if (ErpAuditStatus.APPROVED.getCode().equals(to.getCode())) {

        }
        //审核不通过(设置未通过意见)
        if (ErpAuditStatus.REVOKED.getCode().equals(to.getCode())) {

        }
        //持久化
        ErpPurchaseRequestDO purchaseRequestDO = mapper.selectById(context.getId());
        purchaseRequestDO.setStatus(to.getCode());
        mapper.updateById(purchaseRequestDO);
        log.info("审核状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
    }

    //校验方法
    public void validate(ErpAuditStatus from, ErpAuditStatus to, ErpEventEnum event, ErpPurchaseRequestDO context) {
        //如果是反审核事件
        if (event == ErpEventEnum.WITHDRAW_REVIEW) {
            //不是开启->异常
            ThrowUtil.ifThrow(!ErpOffStatus.OPEN.getCode().equals(context.getOffStatus()), PURCHASE_REQUEST_PROCESS_FAIL_CLOSE);
            //已订购+部分订购->异常
            ThrowUtil.ifThrow(ErpOrderStatus.PARTIALLY_ORDERED.getCode().equals(context.getOrderStatus()) ||
                ErpOrderStatus.ORDERED.getCode().equals(context.getOrderStatus()
                ), PURCHASE_REQUEST_PROCESS_FAIL_ORDERED);
            //设置子表批准数量null
        }
    }
}
