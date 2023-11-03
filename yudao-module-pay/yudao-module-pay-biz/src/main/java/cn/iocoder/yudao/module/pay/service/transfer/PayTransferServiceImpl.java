package cn.iocoder.yudao.module.pay.service.transfer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.framework.pay.core.client.dto.transfer.PayTransferRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.transfer.PayTransferUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.enums.transfer.PayTransferStatusRespEnum;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferCreateReqDTO;
import cn.iocoder.yudao.module.pay.controller.admin.transfer.vo.PayTransferCreateReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.transfer.vo.PayTransferPageReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.channel.PayChannelDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.transfer.PayTransferDO;
import cn.iocoder.yudao.module.pay.dal.mysql.transfer.PayTransferMapper;
import cn.iocoder.yudao.module.pay.dal.redis.no.PayNoRedisDAO;
import cn.iocoder.yudao.module.pay.service.app.PayAppService;
import cn.iocoder.yudao.module.pay.service.channel.PayChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.Validator;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pay.convert.transfer.PayTransferConvert.INSTANCE;
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
    private PayAppService appService;
    @Resource
    private PayChannelService channelService;
    @Resource
    private PayNoRedisDAO noRedisDAO;
    @Resource
    private Validator validator;

    @Override
    public PayTransferDO createTransfer(PayTransferCreateReqVO reqVO, String userIp) {
        // 1. 校验参数
        reqVO.validate(validator);

        // 2. 创建转账单，发起转账
        PayTransferCreateReqDTO req = INSTANCE.convert(reqVO).setUserIp(userIp);
        Long transferId = createTransfer(req);

        // 3. 返回转账单
        return getTransfer(transferId);
    }

    @Override
    public Long createTransfer(PayTransferCreateReqDTO reqDTO) {
        // 1.1 校验转账单是否可以提交
        validateTransferCanCreate(reqDTO.getAppId(), reqDTO.getMerchantTransferId());
        // 1.2 校验 App
        appService.validPayApp(reqDTO.getAppId());
        // 1.3 校验支付渠道是否有效
        PayChannelDO channel = channelService.validPayChannel(reqDTO.getAppId(), reqDTO.getChannelCode());
        PayClient client = channelService.getPayClient(channel.getId());
        if (client == null) {
            log.error("[createTransfer][渠道编号({}) 找不到对应的支付客户端]", channel.getId());
            throw exception(CHANNEL_NOT_FOUND);
        }
        // 2.创建转账单
        String no = noRedisDAO.generate(TRANSFER_NO_PREFIX);
        PayTransferDO transfer = INSTANCE.convert(reqDTO)
                .setChannelId(channel.getId())
                .setNo(no).setStatus(WAITING.getStatus())
                .setNotifyUrl("http://127.0.0.1:48080/admin-api/pay/todo"); // TODO 需要加个transfer Notify url
        transferMapper.insert(transfer);
        PayTransferRespDTO unifiedTransferResp = null;
        try {
            // 3. 调用三方渠道发起转账
            PayTransferUnifiedReqDTO transferUnifiedReq = INSTANCE.convert2(reqDTO)
                    .setOutTransferNo(no);
            unifiedTransferResp = client.unifiedTransfer(transferUnifiedReq);
        } catch (ServiceException ex) {
            // 业务异常.直接返回转账失败的结果
            log.error("[createTransfer][转账 id({}) requestDTO({}) 发生业务异常]", transfer.getId(), reqDTO, ex);
            unifiedTransferResp = PayTransferRespDTO.closedOf("", "", no, ex);
        } catch (Throwable e) {
            // 注意这里仅打印异常，不进行抛出。
            // 原因是：虽然调用支付渠道进行转账发生异常（网络请求超时），实际转账成功。这个结果，后续通过转账回调、或者转账轮询可以拿到。
            // TODO 需要加转账回调业务接口 和 转账轮询未实现
            // 最终，在异常的情况下，支付中心会异步回调业务的转账回调接口，提供转账结果
            log.error("[createTransfer][转账 id({}) requestDTO({}) 发生异常]", transfer.getId(), reqDTO, e);
        }
        if (Objects.nonNull(unifiedTransferResp)) {
            // 4. 通知转账结果
            getSelf().notifyTransfer(channel, unifiedTransferResp);
        }
        return transfer.getId();
    }

    @Override
    public PayTransferDO getTransfer(Long id) {
        return transferMapper.selectById(id);
    }

    @Override
    public PageResult<PayTransferDO> getTransferPage(PayTransferPageReqVO pageReqVO) {
        return transferMapper.selectPage(pageReqVO);
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

    private void validateTransferCanCreate(Long appId, String merchantTransferId) {
        PayTransferDO transfer = transferMapper.selectByAppIdAndMerchantTransferId(appId, merchantTransferId);
        if (transfer != null) {  // 是否存在
            throw exception(PAY_MERCHANT_TRANSFER_EXISTS);
        }
    }

    private void notifyTransferSuccess(PayChannelDO channel, PayTransferRespDTO notify) {
        // 1. 更新 PayTransferDO 转账成功
        Boolean transferred = updateTransferSuccess(channel, notify);
        if (transferred) {
            return;
        }
        // 2. TODO 插入转账通知记录
    }

    private Boolean updateTransferSuccess(PayChannelDO channel, PayTransferRespDTO notify) {
        // 1.校验
        PayTransferDO transfer = transferMapper.selectByNo(notify.getOutTransferNo());
        if (transfer == null) {
            throw exception(PAY_TRANSFER_NOT_FOUND);
        }
        if (isSuccess(transfer.getStatus())) { // 如果已成功，直接返回，不用重复更新
            return Boolean.TRUE;
        }
        if (!isPendingStatus(transfer.getStatus())) {
            throw exception(PAY_TRANSFER_STATUS_IS_NOT_PENDING);
        }
        // 2.更新
        int updateCounts = transferMapper.updateByIdAndStatus(transfer.getId(),
                CollUtil.newArrayList(WAITING.getStatus(), IN_PROGRESS.getStatus()),
                new PayTransferDO().setStatus(SUCCESS.getStatus()).setSuccessTime(notify.getSuccessTime())
                        .setChannelTransferNo(notify.getChannelTransferNo())
                        .setChannelId(channel.getId()).setChannelCode(channel.getCode())
                        .setChannelNotifyData(JsonUtils.toJsonString(notify)));
        if (updateCounts == 0) {
            throw exception(PAY_TRANSFER_STATUS_IS_NOT_PENDING);
        }
        log.info("[updateTransferSuccess][transfer({}) 更新为已转账]", transfer.getId());
        return Boolean.FALSE;
    }

    private void updateTransferClosed(PayChannelDO channel, PayTransferRespDTO notify) {
        // 1.校验
        PayTransferDO transfer = transferMapper.selectByNo(notify.getOutTransferNo());
        if (transfer == null) {
            throw exception(PAY_TRANSFER_NOT_FOUND);
        }
        if (isClosed(transfer.getStatus())) { // 如果已是关闭状态，直接返回，不用重复更新
            log.info("[updateTransferClosed][transfer({}) 已经是关闭状态，无需更新]", transfer.getId());
            return;
        }
        if (!isPendingStatus(transfer.getStatus())) {
            throw exception(PAY_TRANSFER_STATUS_IS_NOT_PENDING);
        }
        // 2.更新
        int updateCount = transferMapper.updateByIdAndStatus(transfer.getId(),
                CollUtil.newArrayList(WAITING.getStatus(), IN_PROGRESS.getStatus()),
                new PayTransferDO().setStatus(CLOSED.getStatus()).setChannelId(channel.getId())
                        .setChannelCode(channel.getCode()).setChannelTransferNo(notify.getChannelTransferNo())
                        .setChannelErrorCode(notify.getChannelErrorCode()).setChannelErrorMsg(notify.getChannelErrorMsg())
                        .setChannelNotifyData(JsonUtils.toJsonString(notify)));
        if (updateCount == 0) {
            throw exception(PAY_TRANSFER_STATUS_IS_NOT_PENDING);
        }
        log.info("[updateTransferClosed][transfer({}) 更新为关闭状态]", transfer.getId());
    }

    private void notifyTransferClosed(PayChannelDO channel, PayTransferRespDTO notify) {
        //  更新 PayTransferDO 转账关闭
        updateTransferClosed(channel, notify);
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
