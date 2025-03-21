package cn.iocoder.yudao.module.erp.config.purchase.request.impl.action;


import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.erp.enums.SrmErrorCodeConstants;
import cn.iocoder.yudao.module.erp.enums.SrmEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpOffStatus;
import cn.iocoder.yudao.module.erp.enums.status.SrmAuditStatus;
import com.alibaba.cola.statemachine.Action;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.DB_UPDATE_ERROR;

@Slf4j
@Component
public class ActionOffImpl implements Action<ErpOffStatus, SrmEventEnum, ErpPurchaseRequestDO> {
    @Resource
    ErpPurchaseRequestMapper mapper;
    @Autowired
    ErpPurchaseRequestItemsMapper itemsMapper;

    //校验方法
    public static void validate(ErpOffStatus from, ErpOffStatus to, SrmEventEnum event, ErpPurchaseRequestDO context) {
        //手动关闭+自动关闭事件
        if (event == SrmEventEnum.MANUAL_CLOSE || event == SrmEventEnum.AUTO_CLOSE) {
            //未审核->异常
            ThrowUtil.ifThrow(Objects.equals(context.getStatus(), SrmAuditStatus.PENDING_REVIEW.getCode()), SrmErrorCodeConstants.PURCHASE_REQUEST_CLOSE_FAIL, context.getNo());
        }
    }

    @Override
    @Transactional
    public void execute(ErpOffStatus from, ErpOffStatus to, SrmEventEnum event, ErpPurchaseRequestDO context) {
        validate(from, to, event, context);
        ErpPurchaseRequestDO aDo = mapper.selectById(context.getId());
        aDo.setOffStatus(to.getCode());
        ThrowUtil.ifSqlThrow(mapper.updateById(aDo), DB_UPDATE_ERROR);
        //ErpPurchaseRequestDO主表开启事件，则主表的子表都开启
        //联动更新子表关闭状态
        if (event == SrmEventEnum.MANUAL_CLOSE || event == SrmEventEnum.AUTO_CLOSE) {
            List<ErpPurchaseRequestItemsDO> itemsDOS = itemsMapper.selectListByRequestId(context.getId());
            itemsDOS.forEach(item -> item.setOffStatus(to.getCode()));
            ThrowUtil.ifThrow(!itemsMapper.updateBatch(itemsDOS), GlobalErrorCodeConstants.DB_BATCH_UPDATE_ERROR);
        }
        //ErpPurchaseRequestDO主表关闭事件，则子表的状态都关闭
        log.debug("开关状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
    }
}