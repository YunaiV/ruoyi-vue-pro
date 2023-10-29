package cn.iocoder.yudao.module.pay.service.transfer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.framework.pay.core.client.dto.transfer.PayTransferRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.transfer.PayTransferUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.enums.channel.PayChannelEnum;
import cn.iocoder.yudao.framework.pay.core.enums.transfer.PayTransferStatusRespEnum;
import cn.iocoder.yudao.framework.pay.core.enums.transfer.PayTransferTypeEnum;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferCreateReqDTO;
import cn.iocoder.yudao.module.pay.controller.admin.transfer.vo.PayTransferSubmitReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.transfer.vo.PayTransferSubmitRespVO;
import cn.iocoder.yudao.module.pay.convert.transfer.PayTransferConvert;
import cn.iocoder.yudao.module.pay.dal.dataobject.channel.PayChannelDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.transfer.PayTransferDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.transfer.PayTransferExtensionDO;
import cn.iocoder.yudao.module.pay.dal.mysql.transfer.PayTransferExtensionMapper;
import cn.iocoder.yudao.module.pay.dal.mysql.transfer.PayTransferMapper;
import cn.iocoder.yudao.module.pay.dal.redis.no.PayNoRedisDAO;
import cn.iocoder.yudao.module.pay.enums.transfer.PayTransferStatusEnum;
import cn.iocoder.yudao.module.pay.service.app.PayAppService;
import cn.iocoder.yudao.module.pay.service.channel.PayChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.pay.enums.transfer.PayTransferStatusEnum.*;

// TODO @jason：等彻底实现完，单测写写；
/**
 * 转账 Service 实现类
 *
 * @author jason
 */
@Service
@Slf4j
public class PayTransferServiceImpl implements PayTransferService {

    private static final String TRANSFER_NO_PREFIX = "T";

    @Resource
    private PayTransferMapper transferMapper;
    @Resource
    private PayTransferExtensionMapper transferExtensionMapper;

    @Resource
    private PayAppService appService;
    @Resource
    private PayChannelService channelService;

    @Resource
    private PayNoRedisDAO noRedisDAO;

    @Override
    public PayTransferSubmitRespVO submitTransfer(PayTransferSubmitReqVO reqVO, String userIp) {
        // 1.1 校验转账单是否可以提交
        PayTransferDO transfer = validateTransferCanSubmit(reqVO.getId());
        // 1.2 校验转账类型和渠道是否匹配
        validateChannelCodeAndTypeMatch(reqVO.getChannelCode(), transfer.getType());
        // 1.3 校验支付渠道是否有效
        PayChannelDO channel = validateChannelCanSubmit(transfer.getAppId(), reqVO.getChannelCode());
        PayClient client = channelService.getPayClient(channel.getId());

        // 2. 新增转账拓展单
        String no = noRedisDAO.generate(TRANSFER_NO_PREFIX);
        PayTransferExtensionDO transferExtension = new PayTransferExtensionDO().setNo(no)
                .setTransferId(transfer.getId()).setChannelId(channel.getId())
                .setChannelCode(channel.getCode()).setStatus(WAITING.getStatus());
        transferExtensionMapper.insert(transferExtension);

        // 3. 调用三方渠道发起转账
        PayTransferUnifiedReqDTO transferUnifiedReq = new PayTransferUnifiedReqDTO()
                .setOutTransferNo(transferExtension.getNo()).setPrice(transfer.getPrice())
                .setType(transfer.getType()).setTitle(transfer.getSubject())
                .setPayeeInfo(transfer.getPayeeInfo()).setUserIp(userIp)
                .setChannelExtras(reqVO.getChannelExtras());
        PayTransferRespDTO unifiedTransferResp = client.unifiedTransfer(transferUnifiedReq);

        // 4. 通知转账结果
        getSelf().notifyTransfer(channel, unifiedTransferResp);
        // 如有渠道错误码，则抛出业务异常，提示用户
        if (StrUtil.isNotEmpty(unifiedTransferResp.getChannelErrorCode())) {
            throw exception(PAY_TRANSFER_SUBMIT_CHANNEL_ERROR, unifiedTransferResp.getChannelErrorCode(),
                    unifiedTransferResp.getChannelErrorMsg());
        }
        return new PayTransferSubmitRespVO().setStatus(unifiedTransferResp.getStatus());
    }

    @Override
    public Long createTransfer(PayTransferCreateReqDTO reqDTO) {
        // 校验 App
        appService.validPayApp(reqDTO.getAppId());
        // 创建转账单
        PayTransferDO transfer = PayTransferConvert.INSTANCE.convert(reqDTO)
                        .setStatus(WAITING.getStatus());
        transferMapper.insert(transfer);
        return transfer.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    // 注意，如果是方法内调用该方法，需要通过 getSelf().notifyTransfer(channel, notify) 调用，否则事务不生效
    public void notifyTransfer(PayChannelDO channel, PayTransferRespDTO notify) {
        // 转账成功的回调
        if (PayTransferStatusRespEnum.isSuccess(notify.getStatus())) {
            notifyTransferSuccess(channel, notify);
        }
        // 转账关闭的回调
        if (PayTransferStatusRespEnum.isClosed(notify.getStatus())) {
            notifyTransferClosed(channel, notify);
        }
        // WAITING 状态无需处理
        // TODO IN_PROGRESS 待处理
    }

    private void notifyTransferSuccess(PayChannelDO channel, PayTransferRespDTO notify) {
        // 1. 更新 PayTransferExtensionDO 转账成功
        PayTransferExtensionDO transferExtension = updateTransferExtensionSuccess(notify);

        // 2. 更新 PayTransferDO 转账成功
        Boolean transferred = updateTransferSuccess(channel,transferExtension, notify);
        if (transferred) {
            return;
        }
        // 3. TODO 插入转账通知记录
    }

    private Boolean updateTransferSuccess(PayChannelDO channel, PayTransferExtensionDO transferExtension,
                                          PayTransferRespDTO notify) {
        // 1.校验
        PayTransferDO transfer = transferMapper.selectById(transferExtension.getTransferId());
        if (transfer == null) {
            throw exception(PAY_TRANSFER_NOT_FOUND);
        }
        if (isSuccess(transfer.getStatus()) && Objects.equals(transfer.getExtensionId(), transferExtension.getId())) {
            log.info("[updateTransferSuccess][transfer({}) 已经是已转账，无需更新]", transfer.getId());
            return true;
        }
        if (!isPendingStatus(transfer.getStatus())) {
            throw exception(PAY_TRANSFER_STATUS_IS_NOT_PENDING);
        }
        // 2.更新
        int updateCounts = transferMapper.updateByIdAndStatus(transfer.getId(),
                CollUtil.newArrayList(WAITING.getStatus(), IN_PROGRESS.getStatus()),
                new PayTransferDO().setStatus(SUCCESS.getStatus()).setSuccessTime(notify.getSuccessTime())
                        .setChannelId(channel.getId()).setChannelCode(channel.getCode())
                        .setExtensionId(transferExtension.getId()).setNo(transferExtension.getNo()));
        if (updateCounts == 0) {
            throw exception(PAY_TRANSFER_STATUS_IS_NOT_PENDING);
        }
        log.info("[updateTransferSuccess][transfer({}) 更新为已转账]", transfer.getId());
        return false;
    }

    private PayTransferExtensionDO updateTransferExtensionSuccess(PayTransferRespDTO notify) {
        // 1 校验
        PayTransferExtensionDO transferExtension = transferExtensionMapper.selectByNo(notify.getOutTransferNo());
        if (transferExtension == null) {
            throw exception(PAY_TRANSFER_EXTENSION_NOT_FOUND);
        }
        if (isSuccess(transferExtension.getStatus())) { // 如果已成功，直接返回，不用重复更新
            log.info("[updateTransferExtensionSuccess][transferExtension({}) 已经是成功状态，无需更新]", transferExtension.getId());
            return transferExtension;
        }
        if (!isPendingStatus(transferExtension.getStatus())) {
            throw exception(PAY_TRANSFER_EXTENSION_STATUS_IS_NOT_PENDING);
        }
        // 2. 更新 PayTransferExtensionDO
        int updateCount = transferExtensionMapper.updateByIdAndStatus(transferExtension.getId(),
                CollUtil.newArrayList(WAITING.getStatus(), IN_PROGRESS.getStatus()),
                new PayTransferExtensionDO().setStatus(SUCCESS.getStatus())
                        .setChannelNotifyData(JsonUtils.toJsonString(notify)));
        if (updateCount == 0) {
            throw exception(PAY_TRANSFER_EXTENSION_STATUS_IS_NOT_PENDING);
        }
        log.info("[updateTransferExtensionSuccess][transferExtension({}) 更新为已转账]", transferExtension.getId());
        return transferExtension;
    }

    private void notifyTransferClosed(PayChannelDO channel, PayTransferRespDTO notify) {
        //  更新 PayTransferExtensionDO 转账关闭
        updateTransferExtensionClosed(notify);
    }

    private void updateTransferExtensionClosed(PayTransferRespDTO notify) {
        // 1 校验
        PayTransferExtensionDO transferExtension = transferExtensionMapper.selectByNo(notify.getOutTransferNo());
        if (transferExtension == null) {
            throw exception(PAY_TRANSFER_EXTENSION_NOT_FOUND);
        }
        if (isClosed(transferExtension.getStatus())) { // 如果已是关闭状态，直接返回，不用重复更新
            log.info("[updateTransferExtensionSuccess][transferExtension({}) 已经是关闭状态，无需更新]", transferExtension.getId());
            return;
        }
        if (!isPendingStatus(transferExtension.getStatus())) {
            throw exception(PAY_TRANSFER_EXTENSION_STATUS_IS_NOT_PENDING);
        }
        // 2. 更新 PayTransferExtensionDO
        int updateCount = transferExtensionMapper.updateByIdAndStatus(transferExtension.getId(),
                CollUtil.newArrayList(WAITING.getStatus(), IN_PROGRESS.getStatus()),
                new PayTransferExtensionDO().setStatus(CLOSED.getStatus())
                        .setChannelNotifyData(JsonUtils.toJsonString(notify)));
        if (updateCount == 0) {
            throw exception(PAY_TRANSFER_EXTENSION_STATUS_IS_NOT_PENDING);
        }
        log.info("[updateTransferExtensionSuccess][transferExtension({}) 更新为关闭状态]", transferExtension.getId());
    }

    private void validateChannelCodeAndTypeMatch(String channelCode, Integer type) {
        PayTransferTypeEnum transferType = PayTransferTypeEnum.typeOf(type);
        PayChannelEnum payChannel = PayChannelEnum.getByCode(channelCode);
        switch (transferType) {
            case ALIPAY_BALANCE: {
                // TODO @jason：可以抽到 PayChannelEnum 里，isAlipay？ 类似这种哈
                if (!payChannel.getCode().startsWith("alipay")) {
                    throw exception(PAY_TRANSFER_TYPE_AND_CHANNEL_NOT_MATCH);
                }
                break;
            }
            case WX_BALANCE:
            case BANK_CARD:
            case WALLET_BALANCE: {
                throw new UnsupportedOperationException("待实现");
            }
        }
    }

    private PayChannelDO validateChannelCanSubmit(Long appId, String channelCode) {
        // 校验 App
        appService.validPayApp(appId);
        // 校验支付渠道是否有效
        PayChannelDO channel = channelService.validPayChannel(appId, channelCode);
        PayClient client = channelService.getPayClient(channel.getId());
        if (client == null) {
            log.error("[validateChannelCanSubmit][渠道编号({}) 找不到对应的支付客户端]", channel.getId());
            throw exception(CHANNEL_NOT_FOUND);
        }
        return channel;
    }

    private PayTransferDO validateTransferCanSubmit(Long id) {
        PayTransferDO transfer = transferMapper.selectById(id);
        if (transfer == null) { // 是否存在
            throw exception(PAY_TRANSFER_NOT_FOUND);
        }
        if (PayTransferStatusEnum.isSuccess(transfer.getStatus())) {
            throw exception(PAY_TRANSFER_STATUS_IS_SUCCESS);
        }
        if (!PayTransferStatusEnum.isWaiting(transfer.getStatus())) {
            throw exception(PAY_TRANSFER_STATUS_IS_NOT_WAITING);
        }
        // TODO 查询拓展单是否未已转账和转账中
        return transfer;
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private PayTransferServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }
}
