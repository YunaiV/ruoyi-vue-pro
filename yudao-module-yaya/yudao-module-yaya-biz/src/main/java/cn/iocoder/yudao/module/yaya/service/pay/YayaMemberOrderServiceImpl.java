package cn.iocoder.yudao.module.yaya.service.pay;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.yaya.controller.app.pay.vo.YayaAppMemberOrderCreateReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.pay.vo.YayaAppMemberOrderRespVO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.member.YayaMemberPlanDO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.pay.YayaMemberOrderDO;
import cn.iocoder.yudao.module.yaya.dal.mysql.member.YayaMemberPlanMapper;
import cn.iocoder.yudao.module.yaya.dal.mysql.pay.YayaMemberOrderMapper;
import cn.iocoder.yudao.module.yaya.service.member.YayaEntitlementService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.addTime;
import static cn.iocoder.yudao.module.yaya.enums.YayaErrorCodeConstants.*;

@Service
@Validated
public class YayaMemberOrderServiceImpl implements YayaMemberOrderService {

    private static final String PAY_APP_KEY = "yaya";
    private static final String STATUS_WAITING_PAYMENT = "WAITING_PAYMENT";
    private static final String STATUS_PAID = "PAID";

    @Resource
    private YayaMemberOrderMapper memberOrderMapper;
    @Resource
    private YayaMemberPlanMapper memberPlanMapper;
    @Resource
    private YayaEntitlementService entitlementService;
    @Resource
    private PayOrderApi payOrderApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public YayaAppMemberOrderRespVO createMemberOrder(Long memberUserId, YayaAppMemberOrderCreateReqVO reqVO,
                                                       String userIp) {
        requireMember(memberUserId);
        entitlementService.ensureDefaultPlans();
        YayaMemberPlanDO plan = memberPlanMapper.selectActiveByPlanKey(reqVO.getPlanKey());
        if (plan == null) {
            throw exception(YAYA_MEMBER_PLAN_NOT_EXISTS);
        }

        YayaMemberOrderDO order = new YayaMemberOrderDO();
        order.setMemberUserId(memberUserId);
        order.setPlanId(plan.getId());
        order.setPlanKey(plan.getPlanKey());
        order.setPriceCent(plan.getPriceCent());
        order.setCurrency(plan.getCurrency());
        order.setStatus(STATUS_WAITING_PAYMENT);
        order.setChannelCode(reqVO.getChannelCode());
        memberOrderMapper.insert(order);

        PayOrderCreateReqDTO payOrderCreateReqDTO = new PayOrderCreateReqDTO();
        payOrderCreateReqDTO.setAppKey(PAY_APP_KEY);
        payOrderCreateReqDTO.setUserIp(StrUtil.blankToDefault(userIp, "127.0.0.1"));
        payOrderCreateReqDTO.setUserId(memberUserId);
        payOrderCreateReqDTO.setUserType(UserTypeEnum.MEMBER.getValue());
        payOrderCreateReqDTO.setMerchantOrderId(order.getId().toString());
        payOrderCreateReqDTO.setSubject(plan.getName());
        payOrderCreateReqDTO.setBody(plan.getDescription());
        payOrderCreateReqDTO.setPrice(plan.getPriceCent().intValue());
        payOrderCreateReqDTO.setExpireTime(addTime(Duration.ofHours(2L)));
        Long payOrderId = payOrderApi.createOrder(payOrderCreateReqDTO);
        YayaMemberOrderDO payOrderUpdate = new YayaMemberOrderDO();
        payOrderUpdate.setId(order.getId());
        payOrderUpdate.setPayOrderId(payOrderId);
        memberOrderMapper.updateById(payOrderUpdate);
        order.setPayOrderId(payOrderId);
        return toResp(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activateEntitlementByPayOrder(Long memberOrderId, Long payOrderId) {
        YayaMemberOrderDO order = memberOrderMapper.selectById(memberOrderId);
        if (order == null) {
            throw exception(YAYA_MEMBER_ORDER_NOT_EXISTS);
        }
        if (!Objects.equals(order.getPayOrderId(), payOrderId)) {
            throw exception(YAYA_MEMBER_ORDER_PAY_MISMATCH);
        }
        if (STATUS_PAID.equals(order.getStatus())) {
            return;
        }

        PayOrderRespDTO payOrder = payOrderApi.getOrder(payOrderId);
        if (payOrder == null || !PayOrderStatusEnum.isSuccess(payOrder.getStatus())) {
            return;
        }
        if (!Objects.equals(payOrder.getMerchantOrderId(), order.getId().toString())
                || payOrder.getPrice() == null
                || !Objects.equals(payOrder.getPrice().longValue(), order.getPriceCent())) {
            throw exception(YAYA_MEMBER_ORDER_PAY_MISMATCH);
        }

        entitlementService.grantEntitlement(order.getMemberUserId(), order.getPlanKey(), "pay_order", payOrderId,
                "pay_order:" + payOrderId);
        YayaMemberOrderDO update = new YayaMemberOrderDO();
        update.setId(order.getId());
        update.setStatus(STATUS_PAID);
        update.setPayChannelCode(payOrder.getChannelCode());
        update.setPaidAt(payOrder.getSuccessTime() == null ? LocalDateTime.now() : payOrder.getSuccessTime());
        memberOrderMapper.updateById(update);
    }

    private static YayaAppMemberOrderRespVO toResp(YayaMemberOrderDO order) {
        return new YayaAppMemberOrderRespVO()
                .setOrderId(order.getId())
                .setPayOrderId(order.getPayOrderId())
                .setStatus(order.getStatus());
    }

    private static void requireMember(Long memberUserId) {
        if (memberUserId == null) {
            throw exception(YAYA_MEMBER_NOT_LOGIN);
        }
    }

}
