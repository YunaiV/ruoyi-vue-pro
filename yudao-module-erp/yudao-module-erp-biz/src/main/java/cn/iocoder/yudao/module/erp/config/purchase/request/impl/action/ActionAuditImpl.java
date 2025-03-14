package cn.iocoder.yudao.module.erp.config.purchase.request.impl.action;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.req.ErpPurchaseRequestAuditReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestItemsMapper;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.DB_BATCH_UPDATE_ERROR;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.DB_UPDATE_ERROR;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_REQUEST_PROCESS_FAIL_CLOSE;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_REQUEST_PROCESS_FAIL_ORDERED;


@Slf4j
@Component
public class ActionAuditImpl implements Action<ErpAuditStatus, ErpEventEnum, ErpPurchaseRequestAuditReqVO> {
    @Autowired
    ErpPurchaseRequestItemsMapper itemsMapper;
    @Autowired
    private ErpPurchaseRequestMapper mapper;

    @Override
    @Transactional
    public void execute(ErpAuditStatus from, ErpAuditStatus to, ErpEventEnum event, ErpPurchaseRequestAuditReqVO req) {
        ErpPurchaseRequestDO data = mapper.selectById(req.getRequestId());
        List<ErpPurchaseRequestItemsDO> itemsDOS = itemsMapper.selectListByRequestId(req.getRequestId());
        validate(from, to, event, data);
        //审核通过(批准数量)
        if (event == ErpEventEnum.AGREE) {
            Map<Long, ErpPurchaseRequestAuditReqVO.requestItems> itemMap = req.getItems().stream()
                .collect(Collectors.toMap(ErpPurchaseRequestAuditReqVO.requestItems::getId, item -> item));
            // 设置批准数量
            itemsDOS.forEach(itemDO -> {
                ErpPurchaseRequestAuditReqVO.requestItems item = itemMap.get(itemDO.getId());
                itemDO.setApproveCount(item.getApproveCount() == null ? itemDO.getCount() : item.getApproveCount());//默认(批准数量 = 申请数量)
            });
            //设置审核意见
            data.setReviewComment(req.getReviewComment());
            data.setAuditTime(LocalDateTime.now());
            data.setAuditorId(getLoginUserId());
        }
        //审核不通过(设置未通过意见)
        if (event == ErpEventEnum.REJECT) {
            data.setReviewComment(req.getReviewComment());
            data.setAuditTime(LocalDateTime.now());
            data.setAuditorId(getLoginUserId());
        }
        //反审核
        if (event == ErpEventEnum.WITHDRAW_REVIEW) {
            //设置审核时间
            data.setAuditTime(LocalDateTime.now());
            data.setAuditorId(getLoginUserId());
        }
        //持久化变更状态
        data.setStatus(to.getCode());
        ThrowUtil.ifThrow(!itemsMapper.updateBatch(itemsDOS), DB_BATCH_UPDATE_ERROR);
        ThrowUtil.ifSqlThrow(mapper.updateById(data), DB_UPDATE_ERROR);
        log.debug("审核状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(data), from.getDesc(), to.getDesc());
    }

    //校验方法
    public void validate(ErpAuditStatus from, ErpAuditStatus to, ErpEventEnum event, ErpPurchaseRequestDO aDo) {
        //如果是反审核事件
        if (event == ErpEventEnum.WITHDRAW_REVIEW) {
            //不是开启->异常
            ThrowUtil.ifThrow(!ErpOffStatus.OPEN.getCode().equals(aDo.getOffStatus()), PURCHASE_REQUEST_PROCESS_FAIL_CLOSE);
            //已订购+部分订购->异常
            ThrowUtil.ifThrow(ErpOrderStatus.PARTIALLY_ORDERED.getCode().equals(aDo.getOrderStatus()) ||
                ErpOrderStatus.ORDERED.getCode().equals(aDo.getOrderStatus()
                ), PURCHASE_REQUEST_PROCESS_FAIL_ORDERED);
        }
    }
}
