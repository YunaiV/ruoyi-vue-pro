package cn.iocoder.yudao.module.pay.service.demo;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pay.api.transfer.PayTransferApi;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferCreateRespDTO;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferRespDTO;
import cn.iocoder.yudao.module.pay.controller.admin.demo.vo.withdraw.PayDemoWithdrawCreateReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.demo.PayDemoWithdrawDO;
import cn.iocoder.yudao.module.pay.dal.mysql.demo.PayDemoWithdrawMapper;
import cn.iocoder.yudao.module.pay.enums.PayChannelEnum;
import cn.iocoder.yudao.module.pay.enums.demo.PayDemoWithdrawStatusEnum;
import cn.iocoder.yudao.module.pay.enums.demo.PayDemoWithdrawTypeEnum;
import cn.iocoder.yudao.module.pay.enums.transfer.PayTransferStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;

/**
 * 示例转账业务 Service 实现类
 *
 * @author jason
 */
@Service
@Validated
@Slf4j
public class PayDemoWithdrawServiceImpl implements PayDemoWithdrawService {

    /**
     * 接入的支付应用标识
     *
     * 从 [支付管理 -> 应用信息] 里添加
     */
    private static final String PAY_APP_KEY = "demo";

    @Resource
    private PayDemoWithdrawMapper demoTransferMapper;

    @Resource
    private PayTransferApi payTransferApi;

    @Override
    public Long createDemoWithdraw(@Valid PayDemoWithdrawCreateReqVO reqVO) {
        PayDemoWithdrawDO withdraw = BeanUtils.toBean(reqVO, PayDemoWithdrawDO.class)
                .setTransferChannelCode(getTransferChannelCode(reqVO.getType()))
                .setStatus(PayDemoWithdrawStatusEnum.WAITING.getStatus());
        demoTransferMapper.insert(withdraw);
        return withdraw.getId();
    }

    @Override
    public Long transferDemoWithdraw(Long id) {
        // 1.1 校验提现单
        PayDemoWithdrawDO withdraw = validateDemoWithdrawCanTransfer(id);
        // 1.2 特殊：如果是转账失败的情况，需要充值下
        if (PayDemoWithdrawStatusEnum.isClosed(withdraw.getStatus())) {
            int updateCount = demoTransferMapper.updateByIdAndStatus(withdraw.getId(), withdraw.getStatus(),
                    new PayDemoWithdrawDO().setStatus(PayDemoWithdrawStatusEnum.WAITING.getStatus()).setTransferErrorMsg(""));
            if (updateCount == 0) {
                throw exception(DEMO_WITHDRAW_TRANSFER_FAIL_STATUS_NOT_WAITING_OR_CLOSED);
            }
            withdraw.setStatus(PayDemoWithdrawStatusEnum.WAITING.getStatus());
        }

        // 2.1 创建支付单
        PayTransferCreateReqDTO transferReqDTO = new PayTransferCreateReqDTO()
                .setAppKey(PAY_APP_KEY).setChannelCode(withdraw.getTransferChannelCode()).setUserIp(getClientIP()) // 支付应用
                .setMerchantTransferId(String.valueOf(withdraw.getId())) // 业务的订单编号
                .setSubject(withdraw.getSubject()).setPrice(withdraw.getPrice()) // 价格信息
                .setUserAccount(withdraw.getUserAccount()).setUserName(withdraw.getUserName()); // 收款信息
        if (ObjectUtil.equal(withdraw.getType(), PayDemoWithdrawTypeEnum.WECHAT.getType())) {
            // 注意：微信很特殊！提现需要写明用途！！！
            transferReqDTO.setChannelExtras(PayTransferCreateReqDTO.buildWeiXinChannelExtra1000(
                    "测试活动", "测试奖励"));
        }
        PayTransferCreateRespDTO transferRespDTO = payTransferApi.createTransfer(transferReqDTO);

        // 2.2 更新转账单到 demo 示例提现单，并将状态更新为转账中
        demoTransferMapper.updateByIdAndStatus(withdraw.getId(), withdraw.getStatus(),
                new PayDemoWithdrawDO().setPayTransferId(transferRespDTO.getId()));
        return transferRespDTO.getId();
    }

    private PayDemoWithdrawDO validateDemoWithdrawCanTransfer(Long id) {
        // 校验存在
        PayDemoWithdrawDO withdraw = demoTransferMapper.selectById(id);
        if (withdraw == null) {
            throw exception(DEMO_WITHDRAW_NOT_FOUND);
        }
        // 校验状态，只有等待中或转账失败的订单，才能发起转账
        if (!PayDemoWithdrawStatusEnum.isWaiting(withdraw.getStatus())
            && !PayDemoWithdrawStatusEnum.isClosed(withdraw.getStatus())) {
            throw exception(DEMO_WITHDRAW_TRANSFER_FAIL_STATUS_NOT_WAITING_OR_CLOSED);
        }
        return withdraw;
    }

    private String getTransferChannelCode(Integer type) {
        if (ObjectUtil.equal(type, PayDemoWithdrawTypeEnum.ALIPAY.getType())) {
            return PayChannelEnum.ALIPAY_PC.getCode();
        }
        if (ObjectUtil.equal(type, PayDemoWithdrawTypeEnum.WECHAT.getType())) {
            return PayChannelEnum.WX_LITE.getCode();
        }
        if (ObjectUtil.equal(type, PayDemoWithdrawTypeEnum.WALLET.getType())) {
            return PayChannelEnum.WALLET.getCode();
        }
        throw new IllegalArgumentException("未知提现方式：" + type);
    }

    @Override
    public PageResult<PayDemoWithdrawDO> getDemoWithdrawPage(PageParam pageVO) {
        return demoTransferMapper.selectPage(pageVO);
    }

    @Override
    public void updateDemoWithdrawTransferred(Long id, Long payTransferId) {
        // 1.1 校验转账单是否存在
        PayDemoWithdrawDO withdraw = demoTransferMapper.selectById(id);
        if (withdraw == null) {
            log.error("[updateDemoWithdrawStatus][withdraw({}) payOrder({}) 不存在提现单，请进行处理！]", id, payTransferId);
            throw exception(DEMO_WITHDRAW_NOT_FOUND);
        }
        // 1.2 校验转账单已成结束（成功或失败）
        if (PayDemoWithdrawStatusEnum.isSuccess(withdraw.getStatus())
            || PayDemoWithdrawStatusEnum.isClosed(withdraw.getStatus())) {
            // 特殊：转账单编号相同，直接返回，说明重复回调
            if (ObjectUtil.equal(withdraw.getPayTransferId(), payTransferId)) {
                log.warn("[updateDemoWithdrawStatus][withdraw({}) 已结束，且转账单编号相同({})，直接返回]", withdraw, payTransferId);
                return;
            }
            // 异常：转账单编号不同，说明转账单编号错误
            log.error("[updateDemoWithdrawStatus][withdraw({}) 转账单不匹配({})，请进行处理！]", withdraw, payTransferId);
            throw exception(DEMO_WITHDRAW_UPDATE_STATUS_FAIL_PAY_TRANSFER_ID_ERROR);
        }

        // 2. 校验提现单的合法性
        PayTransferRespDTO payTransfer = validateDemoTransferStatusCanUpdate(withdraw, payTransferId);

        // 3. 更新示例订单状态
        Integer newStatus = PayTransferStatusEnum.isSuccess(payTransfer.getStatus()) ? PayDemoWithdrawStatusEnum.SUCCESS.getStatus() :
                PayTransferStatusEnum.isClosed(payTransfer.getStatus()) ? PayDemoWithdrawStatusEnum.CLOSED.getStatus() : null;
        Assert.notNull(newStatus, "转账单状态({}) 不合法", payTransfer.getStatus());
        demoTransferMapper.updateByIdAndStatus(withdraw.getId(), withdraw.getStatus(),
                new PayDemoWithdrawDO().setStatus(newStatus).setTransferTime(payTransfer.getSuccessTime())
                        .setTransferErrorMsg(payTransfer.getChannelErrorMsg()));
    }

    private PayTransferRespDTO validateDemoTransferStatusCanUpdate(PayDemoWithdrawDO withdraw, Long payTransferId) {
        // 1. 校验转账单是否存在
        PayTransferRespDTO payTransfer = payTransferApi.getTransfer(payTransferId);
        if (payTransfer == null) {
            log.error("[validateDemoTransferStatusCanUpdate][withdraw({}) payTransfer({}) 不存在，请进行处理！]", withdraw.getId(), payTransferId);
            throw exception(PAY_TRANSFER_NOT_FOUND);
        }

        // 2.1 校验转账单已成功
        if (!PayTransferStatusEnum.isSuccessOrClosed(payTransfer.getStatus())) {
            log.error("[validateDemoTransferStatusCanUpdate][withdraw({}) payTransfer({}) 未结束，请进行处理！payTransfer 数据是：{}]",
                    withdraw.getId(), payTransferId, JsonUtils.toJsonString(payTransfer));
            throw exception(DEMO_WITHDRAW_UPDATE_STATUS_FAIL_PAY_TRANSFER_STATUS_NOT_SUCCESS_OR_CLOSED);
        }
        // 2.2 校验转账金额一致
        if (ObjectUtil.notEqual(payTransfer.getPrice(), withdraw.getPrice())) {
            log.error("[validateDemoTransferStatusCanUpdate][withdraw({}) payTransfer({}) 转账金额不匹配，请进行处理！withdraw 数据是：{}，payTransfer 数据是：{}]",
                    withdraw.getId(), payTransferId, JsonUtils.toJsonString(withdraw), JsonUtils.toJsonString(payTransfer));
            throw exception(DEMO_WITHDRAW_UPDATE_STATUS_FAIL_PAY_PRICE_NOT_MATCH);
        }
        // 2.3 校验转账订单匹配（二次）
        if (ObjectUtil.notEqual(payTransfer.getMerchantTransferId(), withdraw.getId().toString())) {
            log.error("[validateDemoTransferStatusCanUpdate][withdraw({}) 转账单不匹配({})，请进行处理！payTransfer 数据是：{}]",
                    withdraw.getId(), payTransferId, JsonUtils.toJsonString(payTransfer));
            throw exception(DEMO_WITHDRAW_UPDATE_STATUS_FAIL_PAY_MERCHANT_EXISTS);
        }
        // 2.4 校验转账渠道一致
        if (ObjectUtil.notEqual(payTransfer.getChannelCode(), withdraw.getTransferChannelCode())) {
            log.error("[validateDemoTransferStatusCanUpdate][withdraw({}) payTransfer({}) 转账渠道不匹配，请进行处理！withdraw 数据是：{}，payTransfer 数据是：{}]",
                    withdraw.getId(), payTransferId, JsonUtils.toJsonString(withdraw), JsonUtils.toJsonString(payTransfer));
            throw exception(DEMO_WITHDRAW_UPDATE_STATUS_FAIL_PAY_CHANNEL_NOT_MATCH);
        }
        return payTransfer;
    }

}
