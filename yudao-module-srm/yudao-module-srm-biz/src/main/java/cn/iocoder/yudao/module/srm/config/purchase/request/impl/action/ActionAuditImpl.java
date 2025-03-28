package cn.iocoder.yudao.module.srm.config.purchase.request.impl.action;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.request.req.ErpPurchaseRequestAuditReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.ErpPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.ErpPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.ErpOffStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmAuditStatus;
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
public class ActionAuditImpl implements Action<SrmAuditStatus, SrmEventEnum, ErpPurchaseRequestAuditReqVO> {
    @Autowired
    ErpPurchaseRequestItemsMapper itemsMapper;
    @Autowired
    private ErpPurchaseRequestMapper mapper;
    @Autowired
    private ErpPurchaseOrderItemMapper erpPurchaseOrderItemMapper;

    @Override
    @Transactional
    public void execute(SrmAuditStatus from, SrmAuditStatus to, SrmEventEnum event, ErpPurchaseRequestAuditReqVO req) {
        ErpPurchaseRequestDO data = mapper.selectById(req.getRequestId());
        List<ErpPurchaseRequestItemsDO> itemsDOS = itemsMapper.selectListByRequestId(req.getRequestId());
        validate(from, to, event, data, itemsDOS);
        //审核通过(批准数量)
        if (event == SrmEventEnum.AGREE) {
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
        data.setStatus(to.getCode());
        ThrowUtil.ifThrow(!itemsMapper.updateBatch(itemsDOS), DB_BATCH_UPDATE_ERROR);
        ThrowUtil.ifSqlThrow(mapper.updateById(data), DB_UPDATE_ERROR);
        log.debug("审核状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(data), from.getDesc(), to.getDesc());
    }

    //校验方法
    public void validate(SrmAuditStatus from, SrmAuditStatus to, SrmEventEnum event, ErpPurchaseRequestDO aDo, List<ErpPurchaseRequestItemsDO> itemsDOS) {
        //如果是反审核事件
        if (event == SrmEventEnum.WITHDRAW_REVIEW) {
            //不是开启->异常
            ThrowUtil.ifThrow(!ErpOffStatus.OPEN.getCode().equals(aDo.getOffStatus()), PURCHASE_REQUEST_PROCESS_FAIL_CLOSE);
            //已订购+部分订购->异常
//            ThrowUtil.ifThrow(ErpOrderStatus.PARTIALLY_ORDERED.getCode().equals(aDo.getOrderStatus()) ||
//                ErpOrderStatus.ORDERED.getCode().equals(aDo.getOrderStatus()
//                ), PURCHASE_REQUEST_PROCESS_FAIL_ORDERED);
            //判断是否存在对应的采购单
//            itemsDOS 获得它的id集合
            List<Long> ids = itemsDOS.stream().map(ErpPurchaseRequestItemsDO::getId).distinct().toList();
            List<ErpPurchaseOrderItemDO> orderItemDOS = erpPurchaseOrderItemMapper.selectListByPurchaseApplyItemIds(ids);
            //对比差异，报错对应的id
//            if (CollUtil.isNotEmpty(orderItemDOS)) {
//                List<Long> orderItemIds = orderItemDOS.stream().map(ErpPurchaseOrderItemDO::getId).distinct().toList();
//                List<Long> diff = CollUtil.subtract(ids, orderItemIds);
//                ThrowUtil.ifThrow(CollUtil.isNotEmpty(diff), () -> new RuntimeException("存在未关闭的采购单，请先关闭采购单"));
//            }
        }
    }
}
