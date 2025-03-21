package cn.iocoder.yudao.module.erp.config.purchase.order.impl.action;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderAuditReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.iocoder.yudao.module.erp.enums.SrmEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.SrmAuditStatus;
import com.alibaba.cola.statemachine.Action;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.DB_UPDATE_ERROR;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Slf4j
@Component
public class ActionOrderAuditImpl implements Action<SrmAuditStatus, SrmEventEnum, ErpPurchaseOrderAuditReqVO> {
    @Resource
    ErpPurchaseOrderMapper mapper;

    @Override
    @Transactional( rollbackFor = Exception.class)
    public void execute(SrmAuditStatus from, SrmAuditStatus to, SrmEventEnum event, ErpPurchaseOrderAuditReqVO reqVO) {

        List<Long> orderIds = reqVO.getOrderIds();
        //审核第一个
        orderIds.stream().findFirst().ifPresent(id -> {
            ErpPurchaseOrderDO orderDO = mapper.selectById(id);
            orderDO.setAuditStatus(to.getCode());
            //审核人+时间+意见
            if (event == SrmEventEnum.AGREE || event == SrmEventEnum.REJECT) {
                orderDO.setAuditTime(LocalDateTime.now());
                orderDO.setAuditorId(getLoginUserId());
                orderDO.setReviewComment(reqVO.getReviewComment());//审核意见
            }
//            orderDO.setReviewComment(reqVO.getReviewComment()); DB添加字段
            ThrowUtil.ifSqlThrow(mapper.updateById(orderDO), DB_UPDATE_ERROR);
            log.debug("审核状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(reqVO), from.getDesc(), to.getDesc());
        });
    }
}
