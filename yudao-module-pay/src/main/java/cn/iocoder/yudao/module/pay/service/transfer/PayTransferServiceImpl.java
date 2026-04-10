package cn.iocoder.yudao.module.pay.service.transfer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.transfer.PayTransferRespDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.transfer.PayTransferUnifiedReqDTO;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferCreateRespDTO;
import cn.iocoder.yudao.module.pay.controller.admin.transfer.vo.PayTransferPageReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.app.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.channel.PayChannelDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.transfer.PayTransferDO;
import cn.iocoder.yudao.module.pay.dal.mysql.transfer.PayTransferMapper;
import cn.iocoder.yudao.module.pay.dal.redis.no.PayNoRedisDAO;
import cn.iocoder.yudao.module.pay.enums.notify.PayNotifyTypeEnum;
import cn.iocoder.yudao.module.pay.enums.transfer.PayTransferStatusEnum;
import cn.iocoder.yudao.module.pay.framework.pay.config.PayProperties;
import cn.iocoder.yudao.module.pay.service.app.PayAppService;
import cn.iocoder.yudao.module.pay.service.channel.PayChannelService;
import cn.iocoder.yudao.module.pay.service.notify.PayNotifyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;

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
    private PayProperties payProperties;

    @Resource
    private PayTransferMapper transferMapper;
    @Resource
    private PayAppService appService;
    @Resource
    private PayChannelService channelService;
    @Resource
    private PayNotifyService notifyService;
    @Resource
    private PayNoRedisDAO noRedisDAO;

    @Override
    public PayTransferCreateRespDTO createTransfer(PayTransferCreateReqDTO reqDTO) {
        // 1.1 校验 App
        PayAppDO payApp = appService.validPayApp(reqDTO.getAppKey());
        // 1.2 校验支付渠道是否有效
        PayChannelDO channel = channelService.validPayChannel(payApp.getId(), reqDTO.getChannelCode());
        PayClient<?> client = channelService.getPayClient(channel.getId());
        if (client == null) {
            log.error("[createTransfer][渠道编号({}) 找不到对应的支付客户端]", channel.getId());
            throw exception(CHANNEL_NOT_FOUND);
        }
        // 1.3 校验转账单已经发起过转账
        PayTransferDO transfer = validateTransferCanCreate(reqDTO, payApp.getId());

        // 2.1 情况一：不存在创建转账单，则进行创建
        if (transfer == null) {
            String no = noRedisDAO.generate(TRANSFER_NO_PREFIX);
            transfer = BeanUtils.toBean(reqDTO, PayTransferDO.class)
                    .setAppId(channel.getAppId()).setChannelId(channel.getId())
                    .setNo(no).setStatus(PayTransferStatusEnum.WAITING.getStatus())
                    .setNotifyUrl(payApp.getTransferNotifyUrl());
            transferMapper.insert(transfer);
        } else {
            // 2.2 情况二：存在创建转账单，但是状态为关闭，则更新为等待中
            transferMapper.updateByIdAndStatus(transfer.getId(), transfer.getStatus(),
                    new PayTransferDO().setStatus(PayTransferStatusEnum.WAITING.getStatus()));
        }
        PayTransferRespDTO unifiedTransferResp = null;
        try {
            // 3. 调用三方渠道发起转账
            PayTransferUnifiedReqDTO transferUnifiedReq = BeanUtils.toBean(reqDTO, PayTransferUnifiedReqDTO.class)
                    .setOutTransferNo(transfer.getNo())
                    .setNotifyUrl(genChannelTransferNotifyUrl(channel));
            unifiedTransferResp = client.unifiedTransfer(transferUnifiedReq);
            // 4. 通知转账结果
            getSelf().notifyTransfer(channel, unifiedTransferResp);
        } catch (Throwable e) {
            // 注意这里仅打印异常，不进行抛出。
            // 原因是：虽然调用支付渠道进行转账发生异常（网络请求超时），实际转账成功。这个结果，后续转账轮询可以拿到。
            //       或者，使用相同 no 再次发起转账请求
            log.error("[createTransfer][转账编号({}) requestDTO({}) 发生异常]", transfer.getId(), reqDTO, e);
        }
        return new PayTransferCreateRespDTO().setId(transfer.getId())
                .setChannelPackageInfo(unifiedTransferResp != null ? unifiedTransferResp.getChannelPackageInfo() : null);
    }

    /**
     * 根据支付渠道的编码，生成支付渠道的回调地址
     *
     * @param channel 支付渠道
     * @return 支付渠道的回调地址  配置地址 + "/" + channel id
     */
    private String genChannelTransferNotifyUrl(PayChannelDO channel) {
        return payProperties.getTransferNotifyUrl() + "/" + channel.getId();
    }

    private PayTransferDO validateTransferCanCreate(PayTransferCreateReqDTO reqDTO, Long appId) {
        PayTransferDO transfer = transferMapper.selectByAppIdAndMerchantOrderId(appId, reqDTO.getMerchantTransferId());
        if (transfer != null) {
            // 只有转账单状态为关闭，才能再次发起转账
            if (!PayTransferStatusEnum.isClosed(transfer.getStatus())) {
                throw exception(PAY_TRANSFER_CREATE_FAIL_STATUS_NOT_CLOSED);
            }
            // 校验参数是否一致
            if (ObjectUtil.notEqual(reqDTO.getPrice(), transfer.getPrice())) {
                throw exception(PAY_TRANSFER_CREATE_PRICE_NOT_MATCH);
            }
            if (ObjectUtil.notEqual(reqDTO.getChannelCode(), transfer.getChannelCode())) {
                throw exception(PAY_TRANSFER_CREATE_CHANNEL_NOT_MATCH);
            }
        }
        // 如果状态为等待状态：不知道渠道转账是否发起成功
        // 特殊：允许使用相同的 no 再次发起转账，渠道会保证幂等
        return transfer;
    }

    @Transactional(rollbackFor = Exception.class)
    // 注意，如果是方法内调用该方法，需要通过 getSelf().notifyTransfer(channel, notify) 调用，否则事务不生效
    public void notifyTransfer(PayChannelDO channel, PayTransferRespDTO notify) {
        // 转账成功的回调
        if (PayTransferStatusEnum.isSuccess(notify.getStatus())) {
            notifyTransferSuccess(channel, notify);
        }
        // 转账关闭的回调
        if (PayTransferStatusEnum.isClosed(notify.getStatus())) {
            notifyTransferClosed(channel, notify);
        }
        // 转账处理中的回调
        if (PayTransferStatusEnum.isProcessing(notify.getStatus())) {
            notifyTransferProgressing(channel, notify);
        }
        // WAITING 状态无需处理
    }

    private void notifyTransferProgressing(PayChannelDO channel, PayTransferRespDTO notify) {
        // 1. 校验
        PayTransferDO transfer = transferMapper.selectByAppIdAndNo(channel.getAppId(), notify.getOutTransferNo());
        if (transfer == null) {
            throw exception(PAY_TRANSFER_NOT_FOUND);
        }
        if (PayTransferStatusEnum.isProcessing(transfer.getStatus())) { // 如果已经是转账中，直接返回，不用重复更新
            log.info("[notifyTransferProgressing][transfer({}) 已经是转账中状态，无需更新]", transfer.getId());
            return;
        }
        if (!PayTransferStatusEnum.isWaiting(transfer.getStatus())) {
            throw exception(PAY_TRANSFER_NOTIFY_FAIL_STATUS_IS_NOT_WAITING);
        }

        // 2. 更新状态
        int updateCounts = transferMapper.updateByIdAndStatus(transfer.getId(),
                PayTransferStatusEnum.WAITING.getStatus(),
                new PayTransferDO().setStatus(PayTransferStatusEnum.PROCESSING.getStatus())
                        .setChannelPackageInfo(notify.getChannelPackageInfo()));
        if (updateCounts == 0) {
            throw exception(PAY_TRANSFER_NOTIFY_FAIL_STATUS_IS_NOT_WAITING);
        }
        log.info("[notifyTransferProgressing][transfer({}) 更新为转账进行中状态]", transfer.getId());
    }

    private void notifyTransferSuccess(PayChannelDO channel, PayTransferRespDTO notify) {
        // 1. 校验状态
        PayTransferDO transfer = transferMapper.selectByAppIdAndNo(channel.getAppId(), notify.getOutTransferNo());
        if (transfer == null) {
            throw exception(PAY_TRANSFER_NOT_FOUND);
        }
        if (PayTransferStatusEnum.isSuccess(transfer.getStatus())) { // 如果已成功，直接返回，不用重复更新
            log.info("[notifyTransferSuccess][transfer({}) 已经是成功状态，无需更新]", transfer.getId());
            return;
        }
        if (!PayTransferStatusEnum.isWaitingOrProcessing(transfer.getStatus())) {
            throw exception(PAY_TRANSFER_NOTIFY_FAIL_STATUS_NOT_WAITING_OR_PROCESSING);
        }

        // 2. 更新状态
        int updateCounts = transferMapper.updateByIdAndStatus(transfer.getId(),
                CollUtil.newArrayList(PayTransferStatusEnum.WAITING.getStatus(), PayTransferStatusEnum.PROCESSING.getStatus()),
                new PayTransferDO().setStatus(PayTransferStatusEnum.SUCCESS.getStatus())
                        .setSuccessTime(notify.getSuccessTime())
                        .setChannelTransferNo(notify.getChannelTransferNo())
                        .setChannelNotifyData(JsonUtils.toJsonString(notify)));
        if (updateCounts == 0) {
            throw exception(PAY_TRANSFER_NOTIFY_FAIL_STATUS_NOT_WAITING_OR_PROCESSING);
        }
        log.info("[notifyTransferSuccess][transfer({}) 更新为已转账]", transfer.getId());

        // 3. 插入转账通知记录
        notifyService.createPayNotifyTask(PayNotifyTypeEnum.TRANSFER.getType(), transfer.getId());
    }

    private void notifyTransferClosed(PayChannelDO channel, PayTransferRespDTO notify) {
        // 1. 校验状态
        PayTransferDO transfer = transferMapper.selectByAppIdAndNo(channel.getAppId(), notify.getOutTransferNo());
        if (transfer == null) {
            throw exception(PAY_TRANSFER_NOT_FOUND);
        }
        if (PayTransferStatusEnum.isClosed(transfer.getStatus())) { // 如果已是关闭状态，直接返回，不用重复更新
            log.info("[notifyTransferClosed][transfer({}) 已经是关闭状态，无需更新]", transfer.getId());
            return;
        }
        if (!PayTransferStatusEnum.isWaitingOrProcessing(transfer.getStatus())) {
            throw exception(PAY_TRANSFER_NOTIFY_FAIL_STATUS_NOT_WAITING_OR_PROCESSING);
        }

        // 2. 更新状态
        int updateCount = transferMapper.updateByIdAndStatus(transfer.getId(),
                CollUtil.newArrayList(PayTransferStatusEnum.WAITING.getStatus(), PayTransferStatusEnum.PROCESSING.getStatus()),
                new PayTransferDO().setStatus(PayTransferStatusEnum.CLOSED.getStatus())
                        .setChannelTransferNo(notify.getChannelTransferNo())
                        .setChannelNotifyData(JsonUtils.toJsonString(notify))
                        .setChannelErrorCode(notify.getChannelErrorCode()).setChannelErrorMsg(notify.getChannelErrorMsg()));
        if (updateCount == 0) {
            throw exception(PAY_TRANSFER_NOTIFY_FAIL_STATUS_NOT_WAITING_OR_PROCESSING);
        }
        log.info("[notifyTransferClosed][transfer({}) 更新为关闭状态]", transfer.getId());

        // 3. 插入转账通知记录
        notifyService.createPayNotifyTask(PayNotifyTypeEnum.TRANSFER.getType(), transfer.getId());
    }

    @Override
    public PayTransferDO getTransfer(Long id) {
        return transferMapper.selectById(id);
    }

    @Override
    public PayTransferDO getTransferByNo(String no) {
        return transferMapper.selectByNo(no);
    }

    @Override
    public PageResult<PayTransferDO> getTransferPage(PayTransferPageReqVO pageReqVO) {
        return transferMapper.selectPage(pageReqVO);
    }

    @Override
    public int syncTransfer() {
        List<PayTransferDO> list = transferMapper.selectListByStatus(CollUtil.newArrayList(
                PayTransferStatusEnum.WAITING.getStatus(), PayTransferStatusEnum.PROCESSING.getStatus()));
        if (CollUtil.isEmpty(list)) {
            return 0;
        }
        int count = 0;
        for (PayTransferDO transfer : list) {
            count += syncTransfer(transfer) ? 1 : 0;
        }
        return count;
    }

    @Override
    public void syncTransfer(Long id) {
        PayTransferDO transfer = transferMapper.selectById(id);
        if (transfer == null) {
            throw exception(PAY_TRANSFER_NOT_FOUND);
        }
        syncTransfer(transfer);
    }

    private boolean syncTransfer(PayTransferDO transfer) {
        try {
            // 1. 查询转账订单信息
            PayClient<?> payClient = channelService.getPayClient(transfer.getChannelId());
            if (payClient == null) {
                log.error("[syncTransfer][渠道编号({}) 找不到对应的支付客户端]", transfer.getChannelId());
                return false;
            }
            PayTransferRespDTO resp = payClient.getTransfer(transfer.getNo());

            // 2. 回调转账结果
            notifyTransfer(transfer.getChannelId(), resp);
            return true;
        } catch (Throwable ex) {
            log.error("[syncTransfer][transfer({}) 同步转账单状态异常]", transfer.getId(), ex);
            return false;
        }
    }

    public void notifyTransfer(Long channelId, PayTransferRespDTO notify) {
        // 校验渠道是否有效
        PayChannelDO channel = channelService.validPayChannel(channelId);
        // 通知转账结果给对应的业务
        TenantUtils.execute(channel.getTenantId(), () -> getSelf().notifyTransfer(channel, notify));
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
