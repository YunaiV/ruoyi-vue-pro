package cn.iocoder.yudao.module.srm.config.purchase.request.impl.action;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.request.req.SrmPurchaseRequestAuditReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseRequestMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmAuditStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmOffStatus;
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
import static cn.iocoder.yudao.module.srm.enums.SrmErrorCodeConstants.PURCHASE_REQUEST_PROCESS_FAIL_CLOSE;


@Slf4j
@Component
public class AuditActionImpl implements Action<SrmAuditStatus, SrmEventEnum, SrmPurchaseRequestAuditReqVO> {
    @Autowired
    SrmPurchaseRequestItemsMapper itemsMapper;
    @Autowired
    private SrmPurchaseRequestMapper mapper;
    @Autowired
    private SrmPurchaseOrderItemMapper srmPurchaseOrderItemMapper;

    @Override
    @Transactional
    public void execute(SrmAuditStatus from, SrmAuditStatus to, SrmEventEnum event, SrmPurchaseRequestAuditReqVO req) {
        SrmPurchaseRequestDO data = mapper.selectById(req.getRequestId());
        List<SrmPurchaseRequestItemsDO> itemsDOS = itemsMapper.selectListByRequestId(req.getRequestId());
        validate(from, to, event, data, itemsDOS);
        //审核通过(批准数量)
        if (event == SrmEventEnum.AGREE) {
            Map<Long, SrmPurchaseRequestAuditReqVO.requestItems> itemMap = req.getItems().stream()
                .collect(Collectors.toMap(SrmPurchaseRequestAuditReqVO.requestItems::getId, item -> item));
            // 设置批准数量
            itemsDOS.forEach(itemDO -> {
                SrmPurchaseRequestAuditReqVO.requestItems item = itemMap.get(itemDO.getId());
                itemDO.setApproveCount(item.getApproveCount() == null ? itemDO.getQty() : item.getApproveCount());//默认(批准数量 = 申请数量)
            });
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
        //持久化变更状态
        data.setAuditStatus(to.getCode());
        ThrowUtil.ifThrow(!itemsMapper.updateBatch(itemsDOS), DB_BATCH_UPDATE_ERROR);
        ThrowUtil.ifSqlThrow(mapper.updateById(data), DB_UPDATE_ERROR);
        log.debug("审核状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(data), from.getDesc(), to.getDesc());
    }

    //校验方法
    public void validate(SrmAuditStatus from, SrmAuditStatus to, SrmEventEnum event, SrmPurchaseRequestDO aDo, List<SrmPurchaseRequestItemsDO> itemsDOS) {
        //如果是反审核事件
        if (event == SrmEventEnum.WITHDRAW_REVIEW) {
            //不是开启->异常
            ThrowUtil.ifThrow(!SrmOffStatus.OPEN.getCode().equals(aDo.getOffStatus()), PURCHASE_REQUEST_PROCESS_FAIL_CLOSE);
            //已订购+部分订购->异常
//            ThrowUtil.ifThrow(SrmOrderStatus.PARTIALLY_ORDERED.getCode().equals(aDo.getOrderStatus()) ||
//                SrmOrderStatus.ORDERED.getCode().equals(aDo.getOrderStatus()
//                ), PURCHASE_REQUEST_PROCESS_FAIL_ORDERED);
            //判断是否存在对应的采购单
//            itemsDOS 获得它的id集合
            List<Long> ids = itemsDOS.stream().map(SrmPurchaseRequestItemsDO::getId).distinct().toList();
            List<SrmPurchaseOrderItemDO> orderItemDOS = srmPurchaseOrderItemMapper.selectListByPurchaseApplyItemIds(ids);
            //对比差异，报错对应的id
//            if (CollUtil.isNotEmpty(orderItemDOS)) {
//                List<Long> orderItemIds = orderItemDOS.stream().map(SrmPurchaseOrderItemDO::getId).distinct().toList();
//                List<Long> diff = CollUtil.subtract(ids, orderItemIds);
//                ThrowUtil.ifThrow(CollUtil.isNotEmpty(diff), () -> new RuntimeException("存在未关闭的采购单，请先关闭采购单"));
//            }
        }
    }
}
