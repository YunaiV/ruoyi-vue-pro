package cn.iocoder.yudao.module.srm.config.purchase.returned.impl.action;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.returns.SrmPurchaseReturnAuditReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseReturnMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmAuditStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Slf4j
@Component
public class ReturnAuditActionImpl implements Action<SrmAuditStatus, SrmEventEnum, SrmPurchaseReturnAuditReqVO> {

    @Autowired
    SrmPurchaseReturnMapper srmPurchaseReturnMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(SrmAuditStatus from, SrmAuditStatus to, SrmEventEnum event, SrmPurchaseReturnAuditReqVO req) {
        req.getIds().stream().findFirst().ifPresent(id -> {
            SrmPurchaseReturnDO data = srmPurchaseReturnMapper.selectById(id);
            data.setAuditStatus(to.getCode());

            //审核通过(批准数量)
            if (event == SrmEventEnum.AGREE) {
                //设置审核意见
                data.setAuditAdvice(req.getAuditAdvice());
                data.setAuditTime(LocalDateTime.now());
                data.setAuditorId(getLoginUserId());
            }
            //审核不通过(设置未通过意见)
            if (event == SrmEventEnum.REJECT) {
                data.setAuditAdvice(req.getAuditAdvice());
                data.setAuditTime(LocalDateTime.now());
                data.setAuditorId(getLoginUserId());
            }
            //反审核
            if (event == SrmEventEnum.WITHDRAW_REVIEW) {
                //设置审核时间
                data.setAuditTime(LocalDateTime.now());
                data.setAuditorId(getLoginUserId());
            }
            //
            data.setAuditStatus(to.getCode());
            srmPurchaseReturnMapper.updateById(data);
            log.debug("采购退货状态机-审核-触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(data), from.getDesc(), to.getDesc());
        });

    }
}
