package cn.iocoder.yudao.module.pay.service.refund;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.refund.PayRefundRespDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.pay.api.refund.dto.PayRefundCreateReqDTO;
import cn.iocoder.yudao.module.pay.controller.admin.refund.vo.PayRefundExportReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.refund.vo.PayRefundPageReqVO;
import cn.iocoder.yudao.module.pay.convert.refund.PayRefundConvert;
import cn.iocoder.yudao.module.pay.dal.dataobject.app.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.channel.PayChannelDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.refund.PayRefundDO;
import cn.iocoder.yudao.module.pay.dal.mysql.refund.PayRefundMapper;
import cn.iocoder.yudao.module.pay.dal.redis.no.PayNoRedisDAO;
import cn.iocoder.yudao.module.pay.dal.redis.refund.PayRefundLockRedisDAO;
import cn.iocoder.yudao.module.pay.enums.notify.PayNotifyTypeEnum;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.pay.enums.refund.PayRefundStatusEnum;
import cn.iocoder.yudao.module.pay.framework.pay.config.PayProperties;
import cn.iocoder.yudao.module.pay.service.app.PayAppService;
import cn.iocoder.yudao.module.pay.service.channel.PayChannelService;
import cn.iocoder.yudao.module.pay.service.notify.PayNotifyService;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;

/**
 * 退款订单 Service 实现类
 *
 * @author jason
 */
@Service
@Slf4j
@Validated
public class PayRefundServiceImpl implements PayRefundService {

    /**
     * 创建退款单的锁超时时间，单位：毫秒
     */
    private static final long CREATE_LOCK_TIMEOUT_MILLIS = 120 * DateUtils.SECOND_MILLIS;

    /**
     * 结果未知（WAITING）的退款单，距离上一次发起退款的最小间隔，超过后才允许重新发起
     *
     * 注意：只用于避免频繁、并发地重新发起，不作为退款失败的依据。重新发起依赖渠道对原退款单号、原退款金额的幂等
     *
     * @see <a href="https://github.com/YunaiV/ruoyi-vue-pro/issues/1290">#1290</a>
     */
    private static final Duration RETRY_WAITING_INTERVAL = Duration.ofMinutes(10);

    @Resource
    private PayProperties payProperties;

    @Resource
    private PayRefundMapper refundMapper;
    @Resource
    private PayNoRedisDAO noRedisDAO;
    @Resource
    private PayRefundLockRedisDAO refundLockRedisDAO;

    @Resource
    private PayOrderService orderService;
    @Resource
    private PayAppService appService;
    @Resource
    private PayChannelService channelService;
    @Resource
    private PayNotifyService notifyService;

    @Override
    public PayRefundDO getRefund(Long id) {
        return refundMapper.selectById(id);
    }

    @Override
    public PayRefundDO getRefundByNo(String no) {
        return refundMapper.selectByNo(no);
    }

    @Override
    public PayRefundDO getRefundForUpdate(Long id) {
        return refundMapper.selectOneForUpdate(PayRefundDO::getId, id);
    }

    @Override
    public Long getRefundCountByAppId(Long appId) {
        return refundMapper.selectCountByAppId(appId);
    }

    @Override
    public PageResult<PayRefundDO> getRefundPage(PayRefundPageReqVO pageReqVO) {
        return refundMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PayRefundDO> getRefundList(PayRefundExportReqVO exportReqVO) {
        return refundMapper.selectList(exportReqVO);
    }

    @Override
    // 注意：不加入调用方的事务，退款单、退款结果（包括退款通知）各自独立提交。
    // 原因是：调用方的事务回滚（例如说，下面抛出的渠道错误），不能回滚已经向渠道发起的退款单，否则无法使用原退款单号重新发起
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Long createRefund(PayRefundCreateReqDTO reqDTO) {
        // 1.1 校验 App
        PayAppDO app = appService.validPayApp(reqDTO.getAppKey());
        // 1.2 校验支付订单
        PayOrderDO order = validatePayOrderCanRefund(reqDTO, app.getId());
        // 1.3 校验支付渠道是否有效
        PayChannelDO channel = channelService.validPayChannel(order.getChannelId());
        PayClient<?> client = channelService.getPayClient(channel.getId());
        if (client == null) {
            log.error("[createRefund][渠道编号({}) 找不到对应的支付客户端]", channel.getId());
            throw exception(CHANNEL_NOT_FOUND);
        }
        // 2. 情况一：存在结果未知（WAITING）的退款单，先查询渠道退款单，必要时使用原退款单号重新发起
        // 关联 https://github.com/YunaiV/ruoyi-vue-pro/issues/1290、https://github.com/YunaiV/ruoyi-vue-pro/pull/1301
        PayRefundDO refund = refundMapper.selectByAppIdAndMerchantRefundId(app.getId(), reqDTO.getMerchantRefundId());
        if (refund != null && PayRefundStatusEnum.WAITING.getStatus().equals(refund.getStatus())) {
            return retryWaitingRefund(reqDTO, order, channel, client, refund);
        }

        // 3. 情况二：加锁，创建退款单、或者重新发起退款失败的退款单
        // 原因是：同一支付订单串行创建退款单，避免 select 后 insert 时，并发请求重复创建退款单，导致重复退款
        // 注意：createRefund 不加入调用方的事务，退款单立即提交，所以释放锁后，其它请求可以查询到该退款单
        refund = refundLockRedisDAO.lock(order.getId(), CREATE_LOCK_TIMEOUT_MILLIS,
                () -> createOrRetryRefund(reqDTO, app, order));
        // 4. 调用三方渠道发起退款，并通知退款结果
        invokeRefund(channel, client, order, refund, reqDTO.getReason(), false);
        return refund.getId();
    }

    /**
     * 重新发起结果未知（WAITING）的退款单
     *
     * 例如说：发起退款发生异常，并且查询渠道退款单没有明确结果，退款单保留 WAITING。此时，商户使用相同的商户退款编号再次发起：
     * 1. 先查询渠道退款单：退款成功，则同步退款结果；渠道已受理（有渠道退款单号），则继续等待退款回调、或者退款轮询
     * 2. 结果仍未知，并且超过重新发起的间隔，则原子刷新 update_time 占位后，使用原退款单号、原退款金额重新发起
     *
     * 注意：结果未知时，不会将退款单更新为退款失败，避免误判后业务错误处理（例如说，钱包充值退款失败时解冻余额）
     *
     * @param reqDTO 退款申请信息
     * @param order 支付订单
     * @param channel 支付渠道
     * @param client 支付客户端
     * @param refund 结果未知的退款单
     * @return 退款单编号
     * @see <a href="https://github.com/YunaiV/ruoyi-vue-pro/issues/1290">#1290</a>
     */
    private Long retryWaitingRefund(PayRefundCreateReqDTO reqDTO, PayOrderDO order, PayChannelDO channel,
                                    PayClient<?> client, PayRefundDO refund) {
        // 1.1 校验参数是否一致
        validateRefundConsistent(refund, reqDTO, order);
        // 1.2 渠道已受理退款，继续等待退款回调、或者退款轮询，不重新发起
        if (StrUtil.isNotEmpty(refund.getChannelRefundNo())) {
            throw exception(REFUND_HAS_REFUNDING);
        }

        // 2. 查询渠道退款单：退款成功、或者渠道已受理，以查询结果为准，不重新发起
        // 注意：查询到的退款失败、或者查询不到，不作为最终结果。原因是：不能证明原退款请求失败，所以不通知退款失败，交给下面重新发起
        PayRefundRespDTO queryRespDTO = null;
        try {
            queryRespDTO = client.getRefund(refund.getOrderNo(), refund.getNo());
        } catch (Throwable e) {
            log.warn("[retryWaitingRefund][退款编号({}) 查询渠道退款单发生异常]", refund.getId(), e);
        }
        if (isRefundAccepted(queryRespDTO)) {
            getSelf().notifyRefund(channel, queryRespDTO);
            if (PayRefundStatusEnum.isSuccess(queryRespDTO.getStatus())) {
                return refund.getId();
            }
            throw exception(REFUND_HAS_REFUNDING);
        }

        // 3. 原子刷新 update_time 占位：仍是 WAITING、渠道未受理，并且超过重新发起的间隔，保证只有一个请求重新发起
        // 注意：createRefund 不加入调用方的事务，占位立即提交，即使调用方的事务回滚，也不会被撤销，避免短时间内重复发起
        int updateCount = refundMapper.updateUpdateTimeByIdAndWaiting(refund.getId(),
                LocalDateTime.now().minus(RETRY_WAITING_INTERVAL));
        if (updateCount == 0) { // 未超过重新发起的间隔、或者已被并发更新（例如说，退款回调、其它请求重新发起）
            return validateRetryWaitingRefundConcurrent(refund.getId());
        }
        // 4.1 占位后，重新查询退款单，作为本次退款尝试的快照
        // 注意：占位不代表退款结果，占位后仍可能被退款回调更新（例如说，退款成功、写入渠道退款单号），此时以最新状态为准，不再重新发起
        PayRefundDO latestRefund = refundMapper.selectLatestById(refund.getId());
        if (latestRefund == null || !PayRefundStatusEnum.WAITING.getStatus().equals(latestRefund.getStatus())
                || StrUtil.isNotEmpty(latestRefund.getChannelRefundNo())) {
            return validateRetryWaitingRefundConcurrent(refund.getId());
        }
        // 4.2 使用原退款单号、原退款金额、原退款原因重新发起，保证与原退款请求一致，依赖渠道幂等，避免重复退款
        invokeRefund(channel, client, order, latestRefund, latestRefund.getReason(), true);
        return refund.getId();
    }

    /**
     * 重新发起结果未知的退款单时，退款单已被并发更新、或者未超过重新发起的间隔，以最新的退款单为准
     *
     * @param id 退款单编号
     * @return 退款成功时，返回退款单编号；否则，抛出退款处理中的异常
     */
    private Long validateRetryWaitingRefundConcurrent(Long id) {
        PayRefundDO latestRefund = refundMapper.selectLatestById(id);
        if (latestRefund != null && PayRefundStatusEnum.isSuccess(latestRefund.getStatus())) {
            return id;
        }
        throw exception(REFUND_HAS_REFUNDING);
    }

    /**
     * 调用三方渠道发起退款，并通知退款结果
     *
     * @param channel 支付渠道
     * @param client 支付客户端
     * @param order 支付订单
     * @param refund 退款单；需要是发起前查询的最新退款单，它的 updateTime 用于避免旧的退款结果，更新新的退款尝试
     * @param reason 退款原因
     * @param retryWaiting 是否为重新发起结果未知的退款单
     */
    private void invokeRefund(PayChannelDO channel, PayClient<?> client, PayOrderDO order, PayRefundDO refund,
                              String reason, boolean retryWaiting) {
        // 1.1 调用三方渠道发起退款
        PayRefundRespDTO refundRespDTO = null;
        PayRefundUnifiedReqDTO unifiedReqDTO = new PayRefundUnifiedReqDTO()
                .setPayPrice(order.getPrice())
                .setRefundPrice(refund.getRefundPrice())
                .setOutTradeNo(order.getNo())
                .setOutRefundNo(refund.getNo())
                .setNotifyUrl(genChannelRefundNotifyUrl(channel))
                .setReason(reason);
        try {
            refundRespDTO = client.unifiedRefund(unifiedReqDTO);
        } catch (Throwable e) {
            // 注意：这里仅打印异常，不进行抛出。
            // 原因是：虽然调用支付渠道进行退款发生异常（网络请求超时），实际退款成功。
            //        所以下面会查询一次渠道退款单，查询不到明确结果时，后续通过退款回调、或者退款轮询补偿可以拿到
            log.error("[invokeRefund][退款编号({}) unifiedReqDTO({}) 发生异常]", refund.getId(), unifiedReqDTO, e);
        }
        // 1.2 发起退款没有返回结果、返回失败、或者返回结果未知（例如说，支付宝 fund_change 不为 Y）时，查询一次渠道退款单，避免误判
        // 关联知识星球帖子：https://t.zsxq.com/JRLye、https://t.zsxq.com/gDETH
        if (refundRespDTO == null || PayRefundStatusEnum.isFailure(refundRespDTO.getStatus())
                || isRefundUnknown(refundRespDTO)) {
            refundRespDTO = getRefundAfterUnifiedRefund(client, refund, refundRespDTO);
        }

        // 2. 通知退款结果
        if (refundRespDTO == null) {
            return;
        }
        if (PayRefundStatusEnum.isFailure(refundRespDTO.getStatus())) {
            // 2.1 情况一：退款失败
            // 特殊：重新发起结果未知的退款单时，本次返回失败（例如说，渠道对重复请求的报错），不能证明原退款请求失败，
            // 所以不更新为退款失败、不通知业务，保留 WAITING 交给退款回调、退款轮询、或者人工核对后再次发起
            if (retryWaiting) {
                log.warn("[invokeRefund][退款编号({}) 重新发起结果未知的退款单，返回失败({})，保留 WAITING]",
                        refund.getId(), refundRespDTO);
                // 特殊：如果已被并发更新为成功（例如说，退款回调），则以最新状态为准，避免误报
                PayRefundDO latestRefund = refundMapper.selectLatestById(refund.getId());
                if (StrUtil.isNotEmpty(refundRespDTO.getChannelErrorCode())
                        && (latestRefund == null || !PayRefundStatusEnum.isSuccess(latestRefund.getStatus()))) {
                    throw exception(REFUND_SUBMIT_CHANNEL_ERROR, refundRespDTO.getChannelErrorCode(),
                            refundRespDTO.getChannelErrorMsg());
                }
                return;
            }
            PayRefundDO latestRefund = null;
            try {
                getSelf().notifyRefund(channel, refundRespDTO, refund.getUpdateTime());
            } catch (ServiceException ex) {
                // 由于退款回调、退款轮询可能同时更新退款单，导致存在并发更新问题，此时以最新的退款状态为准
                latestRefund = validateRefundNotifyConcurrent(refund.getId(), ex);
                log.warn("[invokeRefund][refund({}) channel({}) 退款结果({}) 通知时发生并发更新，最新状态为({})]",
                        refund.getId(), channel.getId(), refundRespDTO, latestRefund.getStatus(), ex);
            }
            // 如有渠道错误码，则抛出业务异常，提示用户
            // 特殊：如果已被并发更新为成功，则以最新状态为准，避免旧的失败结果误报
            if (StrUtil.isNotEmpty(refundRespDTO.getChannelErrorCode())
                    && (latestRefund == null || PayRefundStatusEnum.isFailure(latestRefund.getStatus()))) {
                throw exception(REFUND_SUBMIT_CHANNEL_ERROR, refundRespDTO.getChannelErrorCode(),
                        refundRespDTO.getChannelErrorMsg());
            }
        } else {
            // 2.2 情况二：退款成功、或者退款中
            // 注意：这里仅打印异常，不进行抛出。原因是：渠道可能已经退款成功，抛出会导致调用方回滚，后续通过退款回调、或者退款轮询补偿
            try {
                getSelf().notifyRefund(channel, refundRespDTO);
            } catch (Throwable e) {
                log.error("[invokeRefund][退款编号({}) 退款结果({}) 通知时发生异常]", refund.getId(), refundRespDTO, e);
            }
        }
    }

    /**
     * 创建退款单；如果存在退款失败的退款单，则重置为待退款，用于重新发起退款
     *
     * @param reqDTO 退款申请信息
     * @param app 支付应用
     * @param order 支付订单
     * @return 退款单；重新查询的最新退款单，作为本次退款尝试的快照
     */
    private PayRefundDO createOrRetryRefund(PayRefundCreateReqDTO reqDTO, PayAppDO app, PayOrderDO order) {
        // 1. 校验退款单是否可以发起：不存在，或者之前退款失败
        PayRefundDO refund = validateRefundCanCreate(reqDTO, app.getId(), order);

        if (refund == null) {
            // 2.1 情况一：不存在退款单，则插入退款单
            String no = noRedisDAO.generate(payProperties.getRefundNoPrefix());
            refund = PayRefundConvert.INSTANCE.convert(reqDTO)
                    .setNo(no).setAppId(app.getId()).setOrderId(order.getId()).setOrderNo(order.getNo())
                    .setChannelId(order.getChannelId()).setChannelCode(order.getChannelCode())
                    // 商户相关的字段
                    .setNotifyUrl(app.getRefundNotifyUrl())
                    // 渠道相关字段
                    .setChannelOrderNo(order.getChannelOrderNo())
                    // 退款相关字段
                    .setStatus(PayRefundStatusEnum.WAITING.getStatus())
                    .setPayPrice(order.getPrice()).setRefundPrice(reqDTO.getPrice());
            refundMapper.insert(refund);
        } else {
            // 2.2 情况二：存在退款失败的退款单，则复用原退款单（包括原退款单号）重新发起，并清空上一次退款的渠道结果
            // 原因是：微信、支付宝等渠道要求退款失败后重新提交时，使用原商户退款单号，避免上一次实际受理时重复退款。未校验一致的请求字段，以本次请求为准
            PayRefundDO updateObj = new PayRefundDO().setStatus(PayRefundStatusEnum.WAITING.getStatus())
                    .setReason(reqDTO.getReason()).setUserIp(reqDTO.getUserIp())
                    .setNotifyUrl(app.getRefundNotifyUrl());
            // 注意：update_time 需要大于退款失败时的 update_time（至少 1 秒，兼容 datetime 秒级精度），用于区分新旧退款尝试，
            // 避免旧的查询结果（例如说，退款轮询查询到的退款失败），更新新的退款尝试，详见 notifyRefund 方法的 waitingUpdateTime 参数
            updateObj.setUpdateTime(DateUtils.max(LocalDateTime.now(), refund.getUpdateTime().plusSeconds(1)));
            int updateCount = refundMapper.updateByIdAndStatusAndClearChannelResult(refund.getId(), refund.getStatus(),
                    updateObj);
            if (updateCount == 0) { // 校验状态，避免并发重新发起退款
                throw exception(REFUND_HAS_REFUNDING);
            }
        }
        return refundMapper.selectById(refund.getId());
    }

    /**
     * 发起退款没有返回结果、返回失败、或者返回结果未知时，查询一次渠道退款单，确认退款结果
     *
     * 注意：只有查询到渠道已受理退款，才以查询结果为准；查询到的退款失败，不作为最终结果。
     * 原因是：一次查询可能存在渠道退款单尚未生成、或者结果尚未同步的情况，例如说微信查询不存在的退款单
     *
     * @param client 支付客户端
     * @param refund 退款单
     * @param unifiedRespDTO 发起退款的返回结果；为 null 时，表示发起退款发生异常
     * @return 退款结果；为 null 时，表示结果未知，保留 WAITING 交给退款回调、或者退款轮询补偿
     */
    private PayRefundRespDTO getRefundAfterUnifiedRefund(PayClient<?> client, PayRefundDO refund, PayRefundRespDTO unifiedRespDTO) {
        PayRefundRespDTO queryRespDTO = null;
        try {
            queryRespDTO = client.getRefund(refund.getOrderNo(), refund.getNo());
        } catch (Throwable e) {
            log.warn("[getRefundAfterUnifiedRefund][退款编号({}) 查询渠道退款单发生异常]", refund.getId(), e);
        }
        // 情况一：查询到退款成功、或者已有渠道退款单号的退款中，说明渠道实际已受理退款，以查询结果为准
        if (isRefundAccepted(queryRespDTO)) {
            return queryRespDTO;
        }
        // 情况二：其它情况，以发起退款的结果为准
        // ① 发起退款发生异常：返回 null，保留 WAITING 交给退款回调、或者退款轮询补偿
        // ② 发起退款返回结果未知：保留 WAITING 交给退款回调、退款轮询、或者商户使用原退款单号重新发起
        // ③ 发起退款明确返回失败：以渠道明确返回的失败为准。原因是：查询不存在的退款单时（例如说，微信、支付宝）返回结果未知，退款轮询无法补偿
        return unifiedRespDTO;
    }

    /**
     * 是否渠道已受理退款：退款成功、或者已有渠道退款单号的退款中
     *
     * @param respDTO 退款结果
     * @return 是否已受理
     */
    private static boolean isRefundAccepted(PayRefundRespDTO respDTO) {
        return respDTO != null && (PayRefundStatusEnum.isSuccess(respDTO.getStatus())
                || (PayRefundStatusEnum.WAITING.getStatus().equals(respDTO.getStatus())
                    && StrUtil.isNotEmpty(respDTO.getChannelRefundNo())));
    }

    /**
     * 是否退款结果未知：退款中，但没有渠道退款单号，例如说，支付宝 fund_change 不为 Y、或者查询不到退款单
     *
     * @param respDTO 退款结果
     * @return 是否未知
     */
    private static boolean isRefundUnknown(PayRefundRespDTO respDTO) {
        return PayRefundStatusEnum.WAITING.getStatus().equals(respDTO.getStatus())
                && StrUtil.isEmpty(respDTO.getChannelRefundNo());
    }

    /**
     * 校验退款结果通知时的并发更新
     *
     * 只有状态竞争导致的通知失败，并且退款单已被并发更新为成功或失败时，才返回最新的退款单；
     * 否则，直接抛出原异常，避免掩盖真实的通知失败
     *
     * @param id 退款单编号
     * @param ex 通知退款结果时的业务异常
     * @return 最新的退款单
     */
    private PayRefundDO validateRefundNotifyConcurrent(Long id, ServiceException ex) {
        // 1. 校验是否为状态竞争
        if (ObjectUtil.notEqual(ex.getCode(), REFUND_STATUS_IS_NOT_WAITING.getCode())) {
            throw ex;
        }
        // 2. 校验最新状态为成功或失败
        PayRefundDO latestRefund = refundMapper.selectLatestById(id);
        if (latestRefund == null || !(PayRefundStatusEnum.isSuccess(latestRefund.getStatus())
                || PayRefundStatusEnum.isFailure(latestRefund.getStatus()))) {
            throw ex;
        }
        return latestRefund;
    }

    /**
     * 校验支付订单是否可以退款
     *
     * @param reqDTO 退款申请信息
     * @return 支付订单
     */
    private PayOrderDO validatePayOrderCanRefund(PayRefundCreateReqDTO reqDTO, Long appId) {
        PayOrderDO order = orderService.getOrder(appId, reqDTO.getMerchantOrderId());
        if (order == null) {
            throw exception(PAY_ORDER_NOT_FOUND);
        }
        // 校验状态，必须是已支付、或者已退款
        if (!PayOrderStatusEnum.isSuccessOrRefund(order.getStatus())) {
            throw exception(PAY_ORDER_REFUND_FAIL_STATUS_ERROR);
        }

        // 校验金额，退款金额不能大于原定的金额
        if (reqDTO.getPrice() + order.getRefundPrice() > order.getPrice()) {
            throw exception(REFUND_PRICE_EXCEED);
        }
        return order;
    }

    /**
     * 校验退款单是否可以发起
     *
     * @param reqDTO 退款申请信息
     * @param appId 应用编号
     * @param order 支付订单
     * @return 退款失败、需要重新发起的退款单；为 null 时，表示需要新建退款单
     */
    private PayRefundDO validateRefundCanCreate(PayRefundCreateReqDTO reqDTO, Long appId, PayOrderDO order) {
        // 是否有退款中的订单。注意：需要在锁内校验，避免并发创建退款单
        if (refundMapper.selectCountByAppIdAndOrderId(appId, order.getId(),
                PayRefundStatusEnum.WAITING.getStatus()) > 0) {
            throw exception(REFUND_HAS_REFUNDING);
        }
        PayRefundDO refund = refundMapper.selectByAppIdAndMerchantRefundId(appId, reqDTO.getMerchantRefundId());
        if (refund == null) {
            return null;
        }
        // 只有退款失败的退款单，才能再次发起退款
        if (!PayRefundStatusEnum.isFailure(refund.getStatus())) {
            throw exception(REFUND_EXISTS);
        }
        // 校验参数是否一致
        validateRefundConsistent(refund, reqDTO, order);
        return refund;
    }

    /**
     * 校验再次发起退款时，原退款单与本次请求的支付订单、退款金额是否一致
     *
     * @param refund 原退款单
     * @param reqDTO 退款申请信息
     * @param order 支付订单
     */
    private void validateRefundConsistent(PayRefundDO refund, PayRefundCreateReqDTO reqDTO, PayOrderDO order) {
        if (ObjectUtil.notEqual(refund.getOrderId(), order.getId())) {
            throw exception(REFUND_CREATE_FAIL_ORDER_NOT_MATCH);
        }
        if (ObjectUtil.notEqual(refund.getRefundPrice(), reqDTO.getPrice())) {
            throw exception(REFUND_CREATE_FAIL_PRICE_NOT_MATCH);
        }
    }

    /**
     * 根据支付渠道的编码，生成支付渠道的回调地址
     *
     * @param channel 支付渠道
     * @return 支付渠道的回调地址  配置地址 + "/" + channel id
     */
    private String genChannelRefundNotifyUrl(PayChannelDO channel) {
        return payProperties.getRefundNotifyUrl() + "/" + channel.getId();
    }

    @Override
    public void notifyRefund(Long channelId, PayRefundRespDTO notify) {
        // 校验支付渠道是否有效
        PayChannelDO channel = channelService.validPayChannel(channelId);
        // 更新退款订单
        TenantUtils.execute(channel.getTenantId(), () -> getSelf().notifyRefund(channel, notify));
    }

    /**
     * 通知并更新订单的退款结果
     *
     * @param channel 支付渠道
     * @param notify  通知
     */
    // 注意，如果是方法内调用该方法，需要通过 getSelf().notifyRefund(channel, notify) 调用，否则事务不生效
    @Transactional(rollbackFor = Exception.class)
    public void notifyRefund(PayChannelDO channel, PayRefundRespDTO notify) {
        notifyRefund(channel, notify, null);
    }

    /**
     * 通知并更新订单的退款结果
     *
     * @param channel 支付渠道
     * @param notify  通知
     * @param waitingUpdateTime 查询（发起）退款前，待退款的退款单的更新时间；非空时，只有退款单的更新时间未变化，才更新为退款失败。
     *                          原因是：退款失败后，可以使用原退款单号重新发起（新的退款尝试），旧的退款失败结果不能关闭新的退款尝试。
     *                          退款成功不需要校验：同一退款单号的退款成功，与哪次退款尝试无关
     */
    // 注意，如果是方法内调用该方法，需要通过 getSelf().notifyRefund(channel, notify, waitingUpdateTime) 调用，否则事务不生效
    @Transactional(rollbackFor = Exception.class)
    public void notifyRefund(PayChannelDO channel, PayRefundRespDTO notify, LocalDateTime waitingUpdateTime) {
        // 情况一：退款成功
        if (PayRefundStatusEnum.isSuccess(notify.getStatus())) {
            notifyRefundSuccess(channel, notify);
            return;
        }
        // 情况二：退款失败
        if (PayRefundStatusEnum.isFailure(notify.getStatus())) {
            notifyRefundFailure(channel, notify, waitingUpdateTime);
            return;
        }
        // 情况三：退款中
        if (PayRefundStatusEnum.WAITING.getStatus().equals(notify.getStatus())) {
            notifyRefundWaiting(channel, notify);
        }
    }

    private void notifyRefundWaiting(PayChannelDO channel, PayRefundRespDTO notify) {
        // 没有渠道退款单号，说明渠道未明确受理退款，无需更新。例如说：支付宝查询不到退款单时返回 WAITING
        if (StrUtil.isEmpty(notify.getChannelRefundNo())) {
            return;
        }
        // 1.1 查询 PayRefundDO
        PayRefundDO refund = refundMapper.selectByAppIdAndNo(
                channel.getAppId(), notify.getOutRefundNo());
        if (refund == null) {
            throw exception(REFUND_NOT_FOUND);
        }
        // 如果不是待退款（例如说，已被退款回调并发更新为最终状态）、或者渠道退款单号已一致，直接返回，不用重复更新
        if (!PayRefundStatusEnum.WAITING.getStatus().equals(refund.getStatus())
                || ObjectUtil.equal(refund.getChannelRefundNo(), notify.getChannelRefundNo())) {
            log.info("[notifyRefundWaiting][退款订单({}) 状态({}) 无需更新渠道退款单号]", refund.getId(), refund.getStatus());
            return;
        }
        // 1.2 更新 PayRefundDO：仍是待退款，补充渠道已受理的退款单号
        // 注意：update_time 作为退款尝试的令牌，不能回退，并且校验读后未被并发更新，详见 notifyRefundFailure 方法
        PayRefundDO updateRefundObj = new PayRefundDO()
                .setChannelRefundNo(notify.getChannelRefundNo()).setChannelNotifyData(toJsonString(notify));
        updateRefundObj.setUpdateTime(DateUtils.max(LocalDateTime.now(), refund.getUpdateTime()));
        int updateCounts = refundMapper.updateByIdAndStatusAndUpdateTime(refund.getId(), refund.getStatus(),
                refund.getUpdateTime(), updateRefundObj);
        if (updateCounts == 0) { // 已被并发更新（例如说，最终状态、占位重新发起），以最新状态为准
            log.info("[notifyRefundWaiting][退款订单({}) 已被并发更新，无需更新渠道退款单号]", refund.getId());
            return;
        }
        log.info("[notifyRefundWaiting][退款订单({}) 更新渠道退款单号({})]", refund.getId(), notify.getChannelRefundNo());
    }

    private void notifyRefundSuccess(PayChannelDO channel, PayRefundRespDTO notify) {
        // 1.1 查询 PayRefundDO
        PayRefundDO refund = refundMapper.selectByAppIdAndNo(
                channel.getAppId(), notify.getOutRefundNo());
        if (refund == null) {
            throw exception(REFUND_NOT_FOUND);
        }
        if (PayRefundStatusEnum.isSuccess(refund.getStatus())) { // 如果已经是成功，直接返回，不用重复更新
            log.info("[notifyRefundSuccess][退款订单({}) 已经是退款成功，无需更新]", refund.getId());
            return;
        }
        if (!PayRefundStatusEnum.WAITING.getStatus().equals(refund.getStatus())) {
            throw exception(REFUND_STATUS_IS_NOT_WAITING);
        }
        // 1.2 更新 PayRefundDO
        PayRefundDO updateRefundObj = new PayRefundDO()
                .setSuccessTime(notify.getSuccessTime())
                .setChannelRefundNo(notify.getChannelRefundNo())
                .setStatus(PayRefundStatusEnum.SUCCESS.getStatus())
                .setChannelNotifyData(toJsonString(notify));
        int updateCounts = refundMapper.updateByIdAndStatus(refund.getId(), refund.getStatus(), updateRefundObj);
        if (updateCounts == 0) { // 校验状态，必须是等待状态
            throw exception(REFUND_STATUS_IS_NOT_WAITING);
        }
        log.info("[notifyRefundSuccess][退款订单({}) 更新为退款成功]", refund.getId());

        // 2. 更新订单
        orderService.updateOrderRefundPrice(refund.getOrderId(), refund.getRefundPrice());

        // 3. 插入退款通知记录
        notifyService.createPayNotifyTask(PayNotifyTypeEnum.REFUND.getType(),
                refund.getId());
    }

    private void notifyRefundFailure(PayChannelDO channel, PayRefundRespDTO notify, LocalDateTime waitingUpdateTime) {
        // 1.1 查询 PayRefundDO
        PayRefundDO refund = refundMapper.selectByAppIdAndNo(
                channel.getAppId(), notify.getOutRefundNo());
        if (refund == null) {
            throw exception(REFUND_NOT_FOUND);
        }
        if (PayRefundStatusEnum.isFailure(refund.getStatus())) { // 如果已经是成功，直接返回，不用重复更新
            log.info("[notifyRefundSuccess][退款订单({}) 已经是退款关闭，无需更新]", refund.getId());
            return;
        }
        if (!PayRefundStatusEnum.WAITING.getStatus().equals(refund.getStatus())) {
            throw exception(REFUND_STATUS_IS_NOT_WAITING);
        }
        // 1.2 更新 PayRefundDO
        // 校验状态，必须是等待状态；并且校验更新时间：如果传入查询（发起）退款前的更新时间，需要是同一次退款尝试，避免关闭重新发起的新退款尝试；
        // 否则，需要是上面查询到的更新时间，避免读后更新期间，退款单被并发更新（例如说，占位重新发起）
        // 注意：update_time 作为退款尝试的令牌，不能回退（重新发起时 update_time 可能大于当前时间），否则同一秒内再次失败、重新发起时，
        //      新的退款尝试会复用旧的令牌，导致旧的退款失败结果关闭新的退款尝试。关联 https://github.com/YunaiV/ruoyi-vue-pro/issues/1290
        LocalDateTime expectedUpdateTime = waitingUpdateTime != null ? waitingUpdateTime : refund.getUpdateTime();
        PayRefundDO updateRefundObj = new PayRefundDO()
                .setChannelRefundNo(notify.getChannelRefundNo())
                .setStatus(PayRefundStatusEnum.FAILURE.getStatus())
                .setChannelNotifyData(toJsonString(notify))
                .setChannelErrorCode(notify.getChannelErrorCode()).setChannelErrorMsg(notify.getChannelErrorMsg());
        updateRefundObj.setUpdateTime(DateUtils.max(LocalDateTime.now(), expectedUpdateTime));
        int updateCounts = refundMapper.updateByIdAndStatusAndUpdateTime(refund.getId(), refund.getStatus(),
                expectedUpdateTime, updateRefundObj);
        if (updateCounts == 0) {
            throw exception(REFUND_STATUS_IS_NOT_WAITING);
        }
        log.info("[notifyRefundFailure][退款订单({}) 更新为退款失败]", refund.getId());

        // 2. 插入退款通知记录
        notifyService.createPayNotifyTask(PayNotifyTypeEnum.REFUND.getType(),
                refund.getId());
    }

    @Override
    public int syncRefund() {
        // 1. 查询指定创建时间内的待退款订单
        List<PayRefundDO> refunds = refundMapper.selectListByStatus(PayRefundStatusEnum.WAITING.getStatus());
        if (CollUtil.isEmpty(refunds)) {
            return 0;
        }
        // 2. 遍历执行
        int count = 0;
        for (PayRefundDO refund : refunds) {
            count += syncRefund(refund) ? 1 : 0;
        }
        return count;
    }

    /**
     * 同步单个退款订单
     *
     * @param refund 退款订单
     * @return 是否同步到
     */
    private boolean syncRefund(PayRefundDO refund) {
        try {
            // 1.1 查询退款订单信息
            PayClient<?> payClient = channelService.getPayClient(refund.getChannelId());
            if (payClient == null) {
                log.error("[syncRefund][渠道编号({}) 找不到对应的支付客户端]", refund.getChannelId());
                return false;
            }
            PayRefundRespDTO respDTO = payClient.getRefund(refund.getOrderNo(), refund.getNo());
            // 1.2 回调退款结果
            // 注意：传入查询前的 updateTime，避免查询期间退款单已失败、并使用原退款单号重新发起时，旧的退款失败结果关闭新的退款尝试
            PayChannelDO channel = channelService.validPayChannel(refund.getChannelId());
            TenantUtils.execute(channel.getTenantId(),
                    () -> getSelf().notifyRefund(channel, respDTO, refund.getUpdateTime()));

            // 2. 如果同步到，则返回 true
            return PayRefundStatusEnum.isSuccess(respDTO.getStatus())
                    || PayRefundStatusEnum.isFailure(respDTO.getStatus());
        } catch (Throwable e) {
            log.error("[syncRefund][refund({}) 同步退款状态异常]", refund.getId(), e);
            return false;
        }
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private PayRefundServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
