package cn.iocoder.yudao.module.srm.config.purchase.order.impl.action;


import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.iocoder.yudao.module.srm.enums.SrmErrorCodeConstants;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.ErpOffStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmAuditStatus;
import com.alibaba.cola.statemachine.Action;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.DB_UPDATE_ERROR;

@Slf4j
@Component
public class ActionOrderOffImpl implements Action<ErpOffStatus, SrmEventEnum, ErpPurchaseOrderDO> {
    @Resource
    ErpPurchaseOrderMapper mapper;


    //校验方法
    public static void validate(ErpOffStatus from, ErpOffStatus to, SrmEventEnum event, ErpPurchaseOrderDO context) {
        //手动关闭+自动关闭事件
        if (event == SrmEventEnum.MANUAL_CLOSE || event == SrmEventEnum.AUTO_CLOSE) {
            //未审核->异常
            ThrowUtil.ifThrow(Objects.equals(context.getAuditStatus(), SrmAuditStatus.PENDING_REVIEW.getCode()), SrmErrorCodeConstants.PURCHASE_ORDER_CLOSE_FAIL, context.getNo());
        }
    }

    @Override
    @Transactional( rollbackFor = Exception.class)
    public void execute(ErpOffStatus from, ErpOffStatus to, SrmEventEnum event, ErpPurchaseOrderDO context) {
        validate(from, to, event, context);
        //子项存在开启主表就开启，子项都关闭|手动关闭主表才关闭
        ErpPurchaseOrderDO aDo = mapper.selectById(context.getId());
        aDo.setOffStatus(to.getCode());
        ThrowUtil.ifSqlThrow(mapper.updateById(aDo), DB_UPDATE_ERROR);
        log.debug("开关状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
    }
}