package cn.iocoder.yudao.module.erp.config.purchase.impl.action;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.req.ErpPurchaseRequestAuditStatusReqVO;
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

import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_REQUEST_PROCESS_FAIL_CLOSE;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_REQUEST_PROCESS_FAIL_ORDERED;


@Slf4j
@Component
public class ActionAuditImpl implements Action<ErpAuditStatus, ErpEventEnum, ErpPurchaseRequestAuditStatusReqVO> {
    @Autowired
    private ErpPurchaseRequestMapper mapper;
    @Autowired
    ErpPurchaseRequestItemsMapper itemsMapper;

    @Override
    @Transactional
    public void execute(ErpAuditStatus from, ErpAuditStatus to, ErpEventEnum event, ErpPurchaseRequestAuditStatusReqVO req) {
        ErpPurchaseRequestDO requestDO = mapper.selectById(req.getRequestId());
        List<ErpPurchaseRequestItemsDO> itemsDOS = itemsMapper.selectListByRequestId(req.getRequestId());
        validate(from, to, event, requestDO);
        //审核通过(批准数量)
        if (ErpAuditStatus.APPROVED.getCode().equals(to.getCode())) {
            Map<Long, ErpPurchaseRequestAuditStatusReqVO.requestItems> itemMap = req.getItems().stream()
                .collect(Collectors.toMap(ErpPurchaseRequestAuditStatusReqVO.requestItems::getId, item -> item));
            // 设置批准数量
            itemsDOS.forEach(itemDO -> {
                ErpPurchaseRequestAuditStatusReqVO.requestItems item = itemMap.get(itemDO.getId());
                if (item != null) {
                    itemDO.setApproveCount(item.getApproveCount());
                } else {
                    itemDO.setApproveCount(itemDO.getCount());//默认申请数量
                }
            });
            //设置审核时间
            requestDO.setAuditTime(LocalDateTime.now());
        }
        //审核不通过(设置未通过意见)
        if (ErpAuditStatus.REVOKED.getCode().equals(to.getCode())) {
            requestDO.setReviewComment(req.getReviewComment());
        }
        //反审核
        if (ErpAuditStatus.DRAFT.getCode().equals(to.getCode())) {
            //设置审核时间
//            requestDO.setAuditTime(null);
        }
        //持久化变更状态
        requestDO.setStatus(to.getCode());
        mapper.updateById(requestDO);
        log.info("审核状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(requestDO), from.getDesc(), to.getDesc());
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
            //设置子表批准数量null
        }
    }
}
