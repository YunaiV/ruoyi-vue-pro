package cn.iocoder.yudao.module.erp.config.purchase.returned.impl.action;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnAuditReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseReturnMapper;
import cn.iocoder.yudao.module.erp.enums.SrmEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.SrmAuditStatus;
import com.alibaba.cola.statemachine.Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Slf4j
@Component
public class ActionRefundAuditImpl implements Action<SrmAuditStatus, SrmEventEnum, ErpPurchaseReturnAuditReqVO> {

    @Autowired
    ErpPurchaseReturnMapper erpPurchaseReturnMapper;


    @Override
    @Transactional
    public void execute(SrmAuditStatus from, SrmAuditStatus to, SrmEventEnum event, ErpPurchaseReturnAuditReqVO req) {
        req.getIds().stream().findFirst().ifPresent(id -> {
            ErpPurchaseReturnDO data = erpPurchaseReturnMapper.selectById(id);
            data.setAuditStatus(to.getCode());

            //审核通过(批准数量)
            if (event == SrmEventEnum.AGREE) {
                //设置审核意见
                data.setReviewComment(req.getReviewComment());
                data.setAuditTime(LocalDateTime.now());
                data.setAuditorId(getLoginUserId());
            }
            //审核不通过(设置未通过意见)
            if (event == SrmEventEnum.REJECT) {
                data.setReviewComment(req.getReviewComment());
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
            erpPurchaseReturnMapper.updateById(data);
            log.debug("审核状态机-退货-触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(data), from.getDesc(), to.getDesc());
        });

    }
}
