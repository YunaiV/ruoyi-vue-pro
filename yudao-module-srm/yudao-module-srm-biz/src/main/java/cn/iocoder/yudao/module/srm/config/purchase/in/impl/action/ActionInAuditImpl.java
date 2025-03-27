package cn.iocoder.yudao.module.srm.config.purchase.in.impl.action;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.ErpPurchaseInAuditReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.ErpPurchaseInItemMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmAuditStatus;
import com.alibaba.cola.statemachine.Action;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.DB_UPDATE_ERROR;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;


@Slf4j
@Component
public class ActionInAuditImpl implements Action<SrmAuditStatus, SrmEventEnum, ErpPurchaseInAuditReqVO> {


    @Resource
    private ErpPurchaseInMapper mapper;
    @Resource
    private ErpPurchaseInItemMapper itemsMapper;

    @Override
    @Transactional
    public void execute(SrmAuditStatus from, SrmAuditStatus to, SrmEventEnum event, ErpPurchaseInAuditReqVO req) {
        ErpPurchaseInDO data = mapper.selectById(req.getInId());
//        List<ErpPurchaseInItemDO> itemDOS = itemsMapper.selectListByInId(req.getInId());
        validate(from, to, event, data);
        //审核通过(批准数量)
        if (event == SrmEventEnum.AGREE) {
//            Map<Long, ErpPurchaseRequestAuditReqVO.requestItems> itemMap = req.getItems().stream()
//                .collect(Collectors.toMap(ErpPurchaseRequestAuditReqVO.requestItems::getId, item -> item));
            // 设置批准数量
//            itemsDOS.forEach(itemDO -> {
//                ErpPurchaseRequestAuditReqVO.requestItems item = itemMap.get(itemDO.getId());
//                itemDO.setApproveCount(item.getApproveCount() == null ? itemDO.getCount() : item.getApproveCount());//默认(批准数量 = 申请数量)
//            });
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
//        ThrowUtil.ifThrow(!itemsMapper.updateBatch(itemsDOS), DB_BATCH_UPDATE_ERROR);
        ThrowUtil.ifSqlThrow(mapper.updateById(data), DB_UPDATE_ERROR);
        log.debug("审核状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(data), from.getDesc(), to.getDesc());
    }

    //校验方法
    public void validate(SrmAuditStatus from, SrmAuditStatus to, SrmEventEnum event, ErpPurchaseInDO aDo) {
//        //如果是反审核事件
//        if (event == SrmEventEnum.WITHDRAW_REVIEW) {
//            //不是开启->异常
//            ThrowUtil.ifThrow(!ErpOffStatus.OPEN.getCode().equals(aDo.getOffStatus()), PURCHASE_REQUEST_PROCESS_FAIL_CLOSE);
//            //已订购+部分订购->异常
//            ThrowUtil.ifThrow(ErpOrderStatus.PARTIALLY_ORDERED.getCode().equals(aDo.getOrderStatus()) ||
//                ErpOrderStatus.ORDERED.getCode().equals(aDo.getOrderStatus()
//                ), PURCHASE_REQUEST_PROCESS_FAIL_ORDERED);
//        }
    }
}
