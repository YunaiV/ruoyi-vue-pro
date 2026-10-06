package cn.iocoder.yudao.module.pay.service.transfer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
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
import cn.iocoder.yudao.module.pay.dal.redis.transfer.PayTransferLockRedisDAO;
import cn.iocoder.yudao.module.pay.enums.notify.PayNotifyTypeEnum;
import cn.iocoder.yudao.module.pay.enums.transfer.PayTransferStatusEnum;
import cn.iocoder.yudao.module.pay.framework.pay.config.PayProperties;
import cn.iocoder.yudao.module.pay.service.app.PayAppService;
import cn.iocoder.yudao.module.pay.service.channel.PayChannelService;
import cn.iocoder.yudao.module.pay.service.notify.PayNotifyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;

/**
 * 转账 Service 实现类
 *
 * @author jason
 */
@Service
@Slf4j
public class PayTransferServiceImpl implements PayTransferService {

    private static final String TRANSFER_NO_PREFIX = "T";

    /**
     * 创建转账单的锁超时时间，单位：毫秒
     */
    private static final long CREATE_LOCK_TIMEOUT_MILLIS = 120 * DateUtils.SECOND_MILLIS;

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
    @Resource
    private PayTransferLockRedisDAO transferLockRedisDAO;

    @Override
    // 注意：不加入调用方的事务，转账单、转账结果（包括转账通知）各自独立提交。
    // 原因是：调用方的事务回滚（例如说，下面抛出的渠道错误），不能回滚已经向渠道发起的转账单，否则无法使用原转账单号重新发起
    // 特殊：钱包渠道查询不到调用方事务未提交的数据，所以钱包创建时独立提交，见 PayWalletServiceImpl#createWalletIfAbsent 方法
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
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
        // 2. 加锁，创建转账单、或者重新发起转账关闭的转账单
        // 原因是：同一商户转账单串行创建转账单，避免 select 后 insert 时，并发请求重复创建转账单，导致重复转账
        // 注意：createTransfer 不加入调用方的事务，转账单立即提交，所以释放锁后，其它请求可以查询到该转账单
        PayTransferDO transfer = transferLockRedisDAO.lock(payApp.getId(), reqDTO.getMerchantTransferId(),
                CREATE_LOCK_TIMEOUT_MILLIS, () -> createOrRetryTransfer(reqDTO, payApp, channel));

        // 3.1 调用三方渠道发起转账
        PayTransferRespDTO unifiedTransferResp = null;
        try {
            PayTransferUnifiedReqDTO transferUnifiedReq = BeanUtils.toBean(reqDTO, PayTransferUnifiedReqDTO.class)
                    .setOutTransferNo(transfer.getNo())
                    .setNotifyUrl(genChannelTransferNotifyUrl(channel));
            unifiedTransferResp = client.unifiedTransfer(transferUnifiedReq);
        } catch (Throwable e) {
            // 注意：这里仅打印异常，不进行抛出。
            // 原因是：虽然调用支付渠道进行转账发生异常（网络请求超时），实际转账成功。
            //        所以下面会查询一次渠道转账单，查询不到明确结果时，后续通过转账回调、或者转账轮询补偿可以拿到
            log.error("[createTransfer][转账编号({}) requestDTO({}) 发生异常]", transfer.getId(), reqDTO, e);
        }
        // 3.2 发起转账没有返回结果、或者返回关闭时，查询一次渠道转账单，避免误判
        if (unifiedTransferResp == null || PayTransferStatusEnum.isClosed(unifiedTransferResp.getStatus())) {
            unifiedTransferResp = getTransferAfterUnifiedTransfer(client, transfer, unifiedTransferResp);
        }

        // 4. 通知转账结果
        if (unifiedTransferResp != null) {
            PayTransferDO latestTransfer = null;
            try {
                getSelf().notifyTransfer(channel, unifiedTransferResp);
            } catch (ServiceException ex) {
                // 由于转账回调、转账轮询可能同时更新转账单，导致存在并发更新问题，此时以最新的转账状态为准
                latestTransfer = validateTransferNotifyConcurrent(transfer.getId(), ex);
                log.warn("[createTransfer][transfer({}) channel({}) 转账结果({}) 通知时发生并发更新，最新状态为({})]",
                        transfer.getId(), channel.getId(), unifiedTransferResp, latestTransfer.getStatus(), ex);
            }
            // 如有渠道错误码，则抛出业务异常，提示用户
            // 特殊：如果已被并发更新为成功，则以最新状态为准，避免旧的关闭结果误报
            if (StrUtil.isNotEmpty(unifiedTransferResp.getChannelErrorCode())
                    && (latestTransfer == null || PayTransferStatusEnum.isClosed(latestTransfer.getStatus()))) {
                throw exception(PAY_TRANSFER_SUBMIT_CHANNEL_ERROR, unifiedTransferResp.getChannelErrorCode(),
                        unifiedTransferResp.getChannelErrorMsg());
            }
        }
        return new PayTransferCreateRespDTO().setId(transfer.getId())
                .setChannelPackageInfo(unifiedTransferResp != null ? unifiedTransferResp.getChannelPackageInfo() : null);
    }

    /**
     * 创建转账单；如果存在转账关闭的转账单，则重置为待转账，用于重新发起转账
     *
     * @param reqDTO 转账申请信息
     * @param payApp 支付应用
     * @param channel 支付渠道
     * @return 转账单
     */
    private PayTransferDO createOrRetryTransfer(PayTransferCreateReqDTO reqDTO, PayAppDO payApp, PayChannelDO channel) {
        // 1. 校验转账单是否可以发起：不存在，或者之前转账关闭
        PayTransferDO transfer = validateTransferCanCreate(reqDTO, payApp.getId());

        if (transfer == null) {
            // 2.1 情况一：不存在转账单，则插入转账单
            String no = noRedisDAO.generate(TRANSFER_NO_PREFIX);
            transfer = BeanUtils.toBean(reqDTO, PayTransferDO.class)
                    .setAppId(channel.getAppId()).setChannelId(channel.getId())
                    .setNo(no).setStatus(PayTransferStatusEnum.WAITING.getStatus())
                    .setNotifyUrl(payApp.getTransferNotifyUrl());
            transferMapper.insert(transfer);
        } else {
            // 2.2 情况二：存在关闭的转账单，则复用原转账单（包括原转账单号）重新发起，并清空上一次转账的渠道结果
            // 原因是：渠道通过转账单号保证幂等，避免上一次实际受理时重复转账。未校验一致的请求字段，以本次请求为准
            // 特殊：渠道编号以本次按渠道编码校验的渠道为准，避免渠道删除重建后，转账轮询、转账回调仍使用旧的渠道编号
            int updateCount = transferMapper.updateByIdAndStatusAndClearChannelResult(transfer.getId(), transfer.getStatus(),
                    new PayTransferDO().setStatus(PayTransferStatusEnum.WAITING.getStatus()).setChannelId(channel.getId())
                            .setSubject(reqDTO.getSubject()).setUserAccount(reqDTO.getUserAccount())
                            .setUserName(reqDTO.getUserName()).setChannelExtras(reqDTO.getChannelExtras())
                            .setUserIp(reqDTO.getUserIp()).setNotifyUrl(payApp.getTransferNotifyUrl()));
            if (updateCount == 0) { // 校验状态，避免并发重新发起转账
                throw exception(PAY_TRANSFER_CREATE_FAIL_STATUS_NOT_CLOSED);
            }
        }
        return transfer;
    }

    /**
     * 发起转账没有返回结果、或者返回关闭时，查询一次渠道转账单，确认转账结果
     *
     * 注意：只有查询到渠道已受理转账，才以查询结果为准；查询到的转账关闭，不作为最终结果。
     * 原因是：一次查询可能存在渠道转账单尚未生成、或者结果尚未同步的情况，例如说钱包查询不存在的转账单时返回关闭
     *
     * @param client 支付客户端
     * @param transfer 转账单
     * @param unifiedResp 发起转账的返回结果；为 null 时，表示发起转账发生异常
     * @return 转账结果；为 null 时，表示结果未知，保留 WAITING 交给转账回调、或者转账轮询补偿
     */
    private PayTransferRespDTO getTransferAfterUnifiedTransfer(PayClient<?> client, PayTransferDO transfer,
                                                               PayTransferRespDTO unifiedResp) {
        PayTransferRespDTO queryResp = null;
        try {
            queryResp = client.getTransfer(transfer.getNo());
        } catch (Throwable e) {
            log.warn("[getTransferAfterUnifiedTransfer][转账编号({}) 查询渠道转账单发生异常]", transfer.getId(), e);
        }
        // 情况一：查询到转账成功、转账中、或者已有渠道转账单号的待转账，说明渠道实际已受理转账，以查询结果为准
        if (queryResp != null && (PayTransferStatusEnum.isSuccess(queryResp.getStatus())
                || PayTransferStatusEnum.isProcessing(queryResp.getStatus())
                || (PayTransferStatusEnum.isWaiting(queryResp.getStatus())
                    && StrUtil.isNotEmpty(queryResp.getChannelTransferNo())))) {
            return queryResp;
        }
        // 情况二：其它情况，以发起转账的结果为准
        // ① 发起转账发生异常：返回 null，保留 WAITING 交给转账回调、或者转账轮询补偿
        // ② 发起转账明确返回关闭：以渠道明确返回的关闭为准。原因是：支付宝查询不存在的转账单时返回 WAITING、微信查询不存在的转账单时抛出异常
        return unifiedResp;
    }

    /**
     * 校验转账结果通知时的并发更新
     *
     * 只有状态竞争导致的通知失败，并且转账单已被并发更新为成功或关闭时，才返回最新的转账单；
     * 否则，直接抛出原异常，避免掩盖真实的通知失败
     *
     * @param id 转账单编号
     * @param ex 通知转账结果时的业务异常
     * @return 最新的转账单
     */
    private PayTransferDO validateTransferNotifyConcurrent(Long id, ServiceException ex) {
        // 1. 校验是否为状态竞争
        if (ObjectUtil.notEqual(ex.getCode(), PAY_TRANSFER_NOTIFY_FAIL_STATUS_IS_NOT_WAITING.getCode())
                && ObjectUtil.notEqual(ex.getCode(), PAY_TRANSFER_NOTIFY_FAIL_STATUS_NOT_WAITING_OR_PROCESSING.getCode())) {
            throw ex;
        }
        // 2. 校验最新状态为成功或关闭
        PayTransferDO latestTransfer = transferMapper.selectById(id);
        if (latestTransfer == null || !PayTransferStatusEnum.isSuccessOrClosed(latestTransfer.getStatus())) {
            throw ex;
        }
        return latestTransfer;
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

    /**
     * 校验转账单是否可以发起
     *
     * @param reqDTO 转账申请信息
     * @param appId 应用编号
     * @return 转账关闭、需要重新发起的转账单；为 null 时，表示需要新建转账单
     */
    private PayTransferDO validateTransferCanCreate(PayTransferCreateReqDTO reqDTO, Long appId) {
        PayTransferDO transfer = transferMapper.selectByAppIdAndMerchantOrderId(appId, reqDTO.getMerchantTransferId());
        if (transfer == null) {
            return null;
        }
        // 只有转账关闭的转账单，才能再次发起转账
        // 原因是：待转账、转账中时，不知道渠道转账是否已经受理，交给转账回调、或者转账轮询补偿
        if (!PayTransferStatusEnum.isClosed(transfer.getStatus())) {
            throw exception(PAY_TRANSFER_CREATE_FAIL_STATUS_NOT_CLOSED);
        }
        // 校验参数是否一致
        if (ObjectUtil.notEqual(reqDTO.getChannelCode(), transfer.getChannelCode())) {
            throw exception(PAY_TRANSFER_CREATE_CHANNEL_NOT_MATCH);
        }
        if (ObjectUtil.notEqual(reqDTO.getPrice(), transfer.getPrice())) {
            throw exception(PAY_TRANSFER_CREATE_PRICE_NOT_MATCH);
        }
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
        // 转账等待的回调
        if (PayTransferStatusEnum.isWaiting(notify.getStatus())) {
            notifyTransferWaiting(channel, notify);
        }
    }

    private void notifyTransferWaiting(PayChannelDO channel, PayTransferRespDTO notify) {
        // 没有渠道转账单号，说明渠道未明确受理转账，无需更新。例如说：支付宝查询不到转账单时返回 WAITING
        if (StrUtil.isEmpty(notify.getChannelTransferNo())) {
            return;
        }
        // 1. 校验
        PayTransferDO transfer = transferMapper.selectByAppIdAndNo(channel.getAppId(), notify.getOutTransferNo());
        if (transfer == null) {
            throw exception(PAY_TRANSFER_NOT_FOUND);
        }
        // 如果不是待转账（例如说，已被转账回调并发更新）、或者渠道转账单号已一致，直接返回，不用重复更新
        if (!PayTransferStatusEnum.isWaiting(transfer.getStatus())
                || ObjectUtil.equal(transfer.getChannelTransferNo(), notify.getChannelTransferNo())) {
            log.info("[notifyTransferWaiting][transfer({}) 状态({}) 无需更新渠道转账单号]", transfer.getId(), transfer.getStatus());
            return;
        }

        // 2. 更新渠道转账单号：仍是待转账，补充渠道已受理的转账单号
        int updateCounts = transferMapper.updateByIdAndStatus(transfer.getId(), PayTransferStatusEnum.WAITING.getStatus(),
                new PayTransferDO().setChannelTransferNo(notify.getChannelTransferNo())
                        .setChannelNotifyData(JsonUtils.toJsonString(notify)));
        if (updateCounts == 0) { // 已被并发更新，以最新状态为准
            log.info("[notifyTransferWaiting][transfer({}) 已被并发更新，无需更新渠道转账单号]", transfer.getId());
            return;
        }
        log.info("[notifyTransferWaiting][transfer({}) 更新渠道转账单号({})]", transfer.getId(), notify.getChannelTransferNo());
    }

    private void notifyTransferProgressing(PayChannelDO channel, PayTransferRespDTO notify) {
        // 1. 校验
        PayTransferDO transfer = transferMapper.selectByAppIdAndNo(channel.getAppId(), notify.getOutTransferNo());
        if (transfer == null) {
            throw exception(PAY_TRANSFER_NOT_FOUND);
        }
        if (PayTransferStatusEnum.isProcessing(transfer.getStatus())) { // 如果已经是转账中，直接返回，不用重复更新
            updateChannelPackageInfoIfAbsent(transfer, notify);
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
                        .setChannelTransferNo(notify.getChannelTransferNo())
                        .setChannelPackageInfo(notify.getChannelPackageInfo()));
        if (updateCounts == 0) {
            PayTransferDO latestTransfer = transferMapper.selectById(transfer.getId());
            if (latestTransfer != null && PayTransferStatusEnum.isProcessing(latestTransfer.getStatus())) {
                updateChannelPackageInfoIfAbsent(latestTransfer, notify);
                log.info("[notifyTransferProgressing][transfer({}) 已被并发更新为转账中状态，无需重复更新]",
                        transfer.getId());
                return;
            }
            throw exception(PAY_TRANSFER_NOTIFY_FAIL_STATUS_IS_NOT_WAITING);
        }
        log.info("[notifyTransferProgressing][transfer({}) 更新为转账进行中状态]", transfer.getId());
    }

    /**
     * 补充渠道 package 信息：处理同步任务先更新为转账中，发起转账接口后返回 channelPackageInfo 的场景
     *
     * @see <a href="https://github.com/YunaiV/ruoyi-vue-pro/issues/1144">Issue #1144</a>
     */
    private void updateChannelPackageInfoIfAbsent(PayTransferDO transfer, PayTransferRespDTO notify) {
        if (StrUtil.isBlank(notify.getChannelPackageInfo())
                || StrUtil.isNotBlank(transfer.getChannelPackageInfo())) {
            return;
        }
        int updateCount = transferMapper.updateChannelPackageInfoIfAbsent(transfer.getId(),
                notify.getChannelPackageInfo());
        if (updateCount > 0) {
            log.info("[updateChannelPackageInfoIfAbsent][transfer({}) 补充渠道 package 信息]", transfer.getId());
        }
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
