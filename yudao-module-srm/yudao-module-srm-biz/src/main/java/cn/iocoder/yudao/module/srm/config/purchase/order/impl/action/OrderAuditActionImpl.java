package cn.iocoder.yudao.module.srm.config.purchase.order.impl.action;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.req.SrmPurchaseOrderAuditReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseOrderMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmAuditStatus;
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
public class OrderAuditActionImpl implements Action<SrmAuditStatus, SrmEventEnum, SrmPurchaseOrderAuditReqVO> {
    @Resource
    SrmPurchaseOrderMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(SrmAuditStatus from, SrmAuditStatus to, SrmEventEnum event, SrmPurchaseOrderAuditReqVO reqVO) {

        List<Long> orderIds = reqVO.getOrderIds();
        //审核第一个
        orderIds.stream().findFirst().ifPresent(id -> {
            SrmPurchaseOrderDO orderDO = mapper.selectById(id);
            orderDO.setAuditStatus(to.getCode());
            //审核人+时间+意见
            if (event == SrmEventEnum.AGREE || event == SrmEventEnum.REJECT) {
                orderDO.setAuditTime(LocalDateTime.now());
                orderDO.setAuditorId(getLoginUserId());
                orderDO.setReviewComment(reqVO.getReviewComment());//审核意见
            }
            //            orderDO.setReviewComment(reqVO.getReviewComment()); DB添加字段
            ThrowUtil.ifSqlThrow(mapper.updateById(orderDO), DB_UPDATE_ERROR);
            log.debug("审核状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(reqVO),
                from.getDesc(), to.getDesc());
        });
    }
}
