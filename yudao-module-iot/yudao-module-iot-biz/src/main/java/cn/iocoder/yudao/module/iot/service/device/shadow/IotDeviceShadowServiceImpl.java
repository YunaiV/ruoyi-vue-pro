package cn.iocoder.yudao.module.iot.service.device.shadow;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.shadow.IotDeviceShadowErrorCodeEnum;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceShadowProtocolUtils;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.shadow.IotDeviceShadowReplyDTO;
import cn.iocoder.yudao.module.iot.core.topic.shadow.IotDeviceShadowUpdateReqDTO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceShadowDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceShadowMapper;
import cn.iocoder.yudao.module.iot.dal.redis.device.DeviceShadowRedisDAO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.core.util.IotDeviceShadowProtocolUtils.INNER_METHOD_GET;
import static cn.iocoder.yudao.module.iot.core.util.IotDeviceShadowProtocolUtils.INNER_METHOD_UPDATE;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.DEVICE_SHADOW_NOT_ENABLED;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.DEVICE_SHADOW_VERSION_CONFLICT;

/**
 * IoT 设备影子 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotDeviceShadowServiceImpl implements IotDeviceShadowService {

    private static final int MAX_PROPERTY_COUNT = 128;
    private static final int OPTIMISTIC_LOCK_RETRY = 3;

    @Resource
    private IotDeviceShadowMapper deviceShadowMapper;
    @Resource
    private DeviceShadowRedisDAO deviceShadowRedisDAO;

    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotDevicePropertyService devicePropertyService;
    // 解决 IotDeviceShadowServiceImpl 与 IotDeviceMessageService 循环依赖
    @Resource
    @Lazy
    private IotDeviceMessageService deviceMessageService;

    @Override
    public IotDeviceShadowDO getOrCreateShadow(IotDeviceDO device) {
        IotDeviceShadowDO shadow = getDeviceShadowFromCacheOrDb(device.getId());
        if (shadow != null) {
            return shadow;
        }
        // 懒插入空文档
        IotDeviceShadowDO newShadow = buildEmptyShadow(device)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .build();
        try {
            deviceShadowMapper.insert(newShadow);
            return newShadow;
        } catch (DuplicateKeyException ex) {
            return deviceShadowMapper.selectByDeviceId(device.getId());
        }
    }

    @Override
    public void handleDeviceShadowUpdate(IotDeviceDO device, IotDeviceMessage message) {
        try {
            handleDeviceShadowUpdate0(device, message);
        } catch (Exception ex) {
            log.error("[handleDeviceShadowUpdate][device({}) message({}) 处理异常]", device.getId(), message, ex);
            Long version = resolveShadowVersionSafely(device.getId());
            sendShadowReplySafely(device, message.getServerId(), IotDeviceShadowReplyDTO.errorOf(version,
                    IotDeviceShadowErrorCodeEnum.SERVER_ERROR.getCode(),
                    IotDeviceShadowErrorCodeEnum.SERVER_ERROR.getMessage(), DateUtil.currentSeconds()));
        }
    }

    private void handleDeviceShadowUpdate0(IotDeviceDO device, IotDeviceMessage message) {
        // 1. 解析请求
        IotDeviceShadowUpdateReqDTO req = JsonUtils.convertObject(message.getParams(), IotDeviceShadowUpdateReqDTO.class);
        if (req == null) {
            sendShadowReplySafely(device, message.getServerId(), IotDeviceShadowReplyDTO.errorOf(0L,
                    IotDeviceShadowErrorCodeEnum.JSON_FORMAT_ERROR.getCode(),
                    IotDeviceShadowErrorCodeEnum.JSON_FORMAT_ERROR.getMessage(), DateUtil.currentSeconds()));
            return;
        }
        if (StrUtil.isEmpty(req.getMethod())) {
            sendShadowReplySafely(device, message.getServerId(), IotDeviceShadowReplyDTO.errorOf(resolveShadowVersionSafely(device.getId()),
                    IotDeviceShadowErrorCodeEnum.METHOD_MISSING.getCode(),
                    IotDeviceShadowErrorCodeEnum.METHOD_MISSING.getMessage(), DateUtil.currentSeconds()));
            return;
        }

        // 2. 获取影子并校验开关
        IotDeviceShadowDO shadow = getOrCreateShadow(device);
        if (CommonStatusEnum.isDisable(shadow.getStatus())) {
            log.info("[handleDeviceShadowUpdate0][device({}) 影子未启用，忽略上行]", device.getId());
            return;
        }

        // 3. 内层 method 分发
        if (INNER_METHOD_GET.equals(req.getMethod())) {
            sendShadowReplySafely(device, message.getServerId(), IotDeviceShadowReplyDTO.getReplyOf(
                    shadow.getDesired(), shadow.getReported(), shadow.getVersion(), shadow.getShadowTime()));
            return;
        }
        if (INNER_METHOD_UPDATE.equals(req.getMethod())) {
            handleReportedUpdate(device, message.getServerId(), req);
            return;
        }
        sendShadowReplySafely(device, message.getServerId(), IotDeviceShadowReplyDTO.errorOf(shadow.getVersion(),
                IotDeviceShadowErrorCodeEnum.INVALID_METHOD.getCode(),
                IotDeviceShadowErrorCodeEnum.INVALID_METHOD.getMessage(), DateUtil.currentSeconds()));
    }

    private void handleReportedUpdate(IotDeviceDO device, String serverId, IotDeviceShadowUpdateReqDTO req) {
        Long deviceId = device.getId();

        if (req.getState() == null) {
            sendShadowReplySafely(device, serverId, IotDeviceShadowReplyDTO.errorOf(resolveShadowVersionSafely(deviceId),
                    IotDeviceShadowErrorCodeEnum.STATE_MISSING.getCode(),
                    IotDeviceShadowErrorCodeEnum.STATE_MISSING.getMessage(), DateUtil.currentSeconds()));
            return;
        }
        if (req.getState().getReported() == null) {
            sendShadowReplySafely(device, serverId, IotDeviceShadowReplyDTO.errorOf(resolveShadowVersionSafely(deviceId),
                    IotDeviceShadowErrorCodeEnum.REPORTED_MISSING.getCode(),
                    IotDeviceShadowErrorCodeEnum.REPORTED_MISSING.getMessage(), DateUtil.currentSeconds()));
            return;
        }
        if (CollUtil.isEmpty(req.getState().getReported())) {
            sendShadowReplySafely(device, serverId, IotDeviceShadowReplyDTO.errorOf(resolveShadowVersionSafely(deviceId),
                    IotDeviceShadowErrorCodeEnum.REPORTED_EMPTY.getCode(),
                    IotDeviceShadowErrorCodeEnum.REPORTED_EMPTY.getMessage(), DateUtil.currentSeconds()));
            return;
        }

        // version == -1：清空影子
        if (req.getVersion() != null && req.getVersion() == -1L) {
            try {
                resetShadowWithRetry(deviceId);
                sendShadowReplySafely(device, serverId, IotDeviceShadowReplyDTO.updateReplyOf(0L, DateUtil.currentSeconds()));
            } catch (ShadowCasExhaustedException ex) {
                handleCasExhaustedForDevice(device, serverId, ex);
            }
            return;
        }

        try {
            IotDeviceShadowDO committed = casUpdateShadow(deviceId, latest -> buildReportedUpdate(latest, req));
            sendShadowReplySafely(device, serverId, IotDeviceShadowReplyDTO.updateReplyOf(committed.getVersion(),
                    committed.getShadowTime()));
        } catch (ShadowUpdateRejectedException ex) {
            sendShadowReplySafely(device, serverId, IotDeviceShadowReplyDTO.errorOf(ex.getVersion(),
                    ex.getErrorCode().getCode(), ex.getErrorCode().getMessage(), DateUtil.currentSeconds()));
        } catch (ShadowCasExhaustedException ex) {
            handleCasExhaustedForDevice(device, serverId, ex);
        }
    }

    @Override
    public void updateDesiredState(Long deviceId, Map<String, Object> desired) {
        IotDeviceDO device = deviceService.validateDeviceExists(deviceId);
        IotDeviceShadowDO shadow = getOrCreateShadow(device);
        if (CommonStatusEnum.isDisable(shadow.getStatus())) {
            throw exception(DEVICE_SHADOW_NOT_ENABLED);
        }
        if (CollUtil.isEmpty(desired)) {
            return;
        }

        try {
            IotDeviceShadowDO committed = casUpdateShadow(deviceId, latest -> buildDesiredUpdate(latest, desired));
            // 设备在线则主动推送全量影子（复用 CAS 返回值，避免二次查库）
            String serverId = devicePropertyService.getDeviceServerId(deviceId);
            if (StrUtil.isNotEmpty(serverId)) {
                sendShadowReplySafely(device, serverId, IotDeviceShadowReplyDTO.getReplyOf(
                        committed.getDesired(), committed.getReported(),
                        committed.getVersion(), committed.getShadowTime()));
            }
        } catch (ShadowCasExhaustedException ex) {
            throw exception(DEVICE_SHADOW_VERSION_CONFLICT);
        }
    }

    @Override
    public void resetShadow(Long deviceId) {
        deviceService.validateDeviceExists(deviceId);
        IotDeviceShadowDO shadow = getDeviceShadowFromCacheOrDb(deviceId);
        if (shadow == null) {
            return;
        }
        try {
            resetShadowWithRetry(deviceId);
        } catch (ShadowCasExhaustedException ex) {
            throw exception(DEVICE_SHADOW_VERSION_CONFLICT);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long deviceId, Integer status) {
        IotDeviceDO device = deviceService.validateDeviceExists(deviceId);
        IotDeviceShadowDO shadow = getDeviceShadowFromCacheOrDb(deviceId);
        if (shadow == null) {
            // 影子行不存在时直接按目标 status 懒建，避免后续 getOrCreateShadow 始终以 ENABLE 插入
            IotDeviceShadowDO newShadow = buildEmptyShadow(device).status(status).build();
            try {
                deviceShadowMapper.insert(newShadow);
            } catch (DuplicateKeyException ex) {
                int rows = deviceShadowMapper.updateStatusByDeviceId(deviceId, status);
                if (rows == 0) {
                    throw exception(DEVICE_SHADOW_VERSION_CONFLICT);
                }
                deviceShadowRedisDAO.delete(deviceId);
            }
        } else {
            int rows = deviceShadowMapper.updateStatusByDeviceId(deviceId, status);
            if (rows == 0) {
                throw exception(DEVICE_SHADOW_VERSION_CONFLICT);
            }
            deviceShadowRedisDAO.delete(deviceId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByDeviceId(Long deviceId) {
        deviceShadowMapper.deleteByDeviceId(deviceId);
        deviceShadowRedisDAO.delete(deviceId);
    }

    // ========== 内部方法 ==========

    private IotDeviceShadowDO buildReportedUpdate(IotDeviceShadowDO latest, IotDeviceShadowUpdateReqDTO req) {
        // 乐观锁：带 version 时必须与当前完全一致（null=强制更新、-1=清空已在上层处理）
        if (req.getVersion() != null && !req.getVersion().equals(latest.getVersion())) {
            throw new ShadowUpdateRejectedException(latest.getVersion(), IotDeviceShadowErrorCodeEnum.VERSION_CONFLICT);
        }
        Map<String, Object> mergedReported = mergeMap(latest.getReported(), req.getState().getReported());
        if (mergedReported.size() > MAX_PROPERTY_COUNT) {
            throw new ShadowUpdateRejectedException(latest.getVersion(), IotDeviceShadowErrorCodeEnum.PROPERTY_COUNT_EXCEEDED);
        }
        long now = DateUtil.currentSeconds();
        Map<String, Object> metadata = latest.getMetadata() != null ? new HashMap<>(latest.getMetadata()) : new HashMap<>();
        IotDeviceShadowProtocolUtils.putReportedTimestamps(metadata, req.getState().getReported().keySet(), now);
        return IotDeviceShadowDO.builder()
                .reported(mergedReported)
                .metadata(metadata)
                .version(latest.getVersion() + 1)
                .shadowTime(now)
                .build();
    }

    private IotDeviceShadowDO buildDesiredUpdate(IotDeviceShadowDO latest, Map<String, Object> desired) {
        long now = DateUtil.currentSeconds();
        Map<String, Object> mergedDesired = mergeMap(latest.getDesired(), desired);
        Map<String, Object> metadata = latest.getMetadata() != null ? new HashMap<>(latest.getMetadata()) : new HashMap<>();
        IotDeviceShadowProtocolUtils.putDesiredTimestamps(metadata, desired.keySet(), now);
        return IotDeviceShadowDO.builder()
                .desired(mergedDesired)
                .metadata(metadata)
                .version(latest.getVersion() + 1)
                .shadowTime(now)
                .build();
    }

    private void resetShadowWithRetry(Long deviceId) {
        casUpdateShadow(deviceId, latest -> IotDeviceShadowDO.builder()
                .desired(new HashMap<>())
                .reported(new HashMap<>())
                .metadata(new HashMap<>())
                .version(0L)
                .shadowTime(DateUtil.currentSeconds())
                .build());
    }

    /**
     * 乐观锁 CAS 更新：每次重试基于最新影子重新构建 update，避免 lost-update
     *
     * @return 写入成功后的 update DO（回填未更新字段，便于构建全量回复）
     */
    private IotDeviceShadowDO casUpdateShadow(Long deviceId, Function<IotDeviceShadowDO, IotDeviceShadowDO> updateBuilder) {
        Long lastKnownVersion = 0L;
        for (int i = 0; i < OPTIMISTIC_LOCK_RETRY; i++) {
            // 首次尝试走缓存，重试时直读 DB 获取最新 version
            IotDeviceShadowDO latest = i == 0
                    ? getDeviceShadowFromCacheOrDb(deviceId)
                    : deviceShadowMapper.selectByDeviceId(deviceId);
            if (latest == null) {
                throw ShadowCasExhaustedException.shadowNotFound();
            }
            lastKnownVersion = latest.getVersion();
            IotDeviceShadowDO update = updateBuilder.apply(latest);
            int rows = deviceShadowMapper.updateByDeviceIdAndVersion(update, deviceId, latest.getVersion());
            if (rows > 0) {
                // cache-aside：写后失效 Redis 读缓存
                deviceShadowRedisDAO.delete(deviceId);
                enrichCommittedShadow(update, latest);
                return update;
            }
        }
        throw ShadowCasExhaustedException.versionConflict(lastKnownVersion);
    }

    /**
     * 将 CAS 成功写入的局部字段与 latest 快照合并，供全量 get_reply 使用
     */
    private static void enrichCommittedShadow(IotDeviceShadowDO committed, IotDeviceShadowDO latest) {
        if (committed.getDesired() == null) {
            committed.setDesired(latest.getDesired());
        }
        if (committed.getReported() == null) {
            committed.setReported(latest.getReported());
        }
        if (committed.getMetadata() == null) {
            committed.setMetadata(latest.getMetadata());
        }
    }

    private void handleCasExhaustedForDevice(IotDeviceDO device, String serverId, ShadowCasExhaustedException ex) {
        if (ex.isShadowNotFound()) {
            sendShadowReplySafely(device, serverId, IotDeviceShadowReplyDTO.errorOf(0L,
                    IotDeviceShadowErrorCodeEnum.SERVER_ERROR.getCode(),
                    IotDeviceShadowErrorCodeEnum.SERVER_ERROR.getMessage(), DateUtil.currentSeconds()));
            return;
        }
        sendVersionConflictReply(device, serverId, ex.getLastKnownVersion());
    }

    private void sendVersionConflictReply(IotDeviceDO device, String serverId, Long version) {
        sendShadowReplySafely(device, serverId, IotDeviceShadowReplyDTO.errorOf(version,
                IotDeviceShadowErrorCodeEnum.VERSION_CONFLICT.getCode(),
                IotDeviceShadowErrorCodeEnum.VERSION_CONFLICT.getMessage(), DateUtil.currentSeconds()));
    }

    private Long resolveShadowVersionSafely(Long deviceId) {
        try {
            IotDeviceShadowDO shadow = getDeviceShadowFromCacheOrDb(deviceId);
            return shadow != null ? shadow.getVersion() : 0L;
        } catch (Exception ex) {
            log.warn("[resolveShadowVersionSafely][device({}) 读取 version 失败，使用保底 0]", deviceId, ex);
            return 0L;
        }
    }

    private IotDeviceShadowDO.IotDeviceShadowDOBuilder buildEmptyShadow(IotDeviceDO device) {
        return IotDeviceShadowDO.builder()
                .deviceId(device.getId())
                .productId(device.getProductId())
                .productKey(device.getProductKey())
                .deviceName(device.getDeviceName())
                .reported(new HashMap<>())
                .desired(new HashMap<>())
                .metadata(new HashMap<>())
                .version(0L)
                .shadowTime(DateUtil.currentSeconds());
    }

    /**
     * cache-aside 读路径：Redis 命中则返回，否则查 DB 并回填缓存
     */
    private IotDeviceShadowDO getDeviceShadowFromCacheOrDb(Long deviceId) {
        IotDeviceShadowDO shadow = deviceShadowRedisDAO.get(deviceId);
        if (shadow != null) {
            return shadow;
        }
        shadow = deviceShadowMapper.selectByDeviceId(deviceId);
        if (shadow != null) {
            deviceShadowRedisDAO.set(shadow);
        }
        return shadow;
    }

    /**
     * 下发影子回复，内部吞掉异常，保证上行处理链路不向上抛
     */
    private void sendShadowReplySafely(IotDeviceDO device, String serverId, IotDeviceShadowReplyDTO replyDTO) {
        IotDeviceMessage shadowGetMsg = IotDeviceMessage.requestOf(
                device.getId(), device.getTenantId(), serverId,
                IotDeviceMessageMethodEnum.SHADOW_GET.getMethod(), replyDTO);
        try {
            deviceMessageService.sendDeviceMessage(shadowGetMsg, device, serverId);
        } catch (Exception ex) {
            log.error("[sendShadowReply][device({}) 下发影子回复失败]", device.getId(), ex);
        }
    }

    private static Map<String, Object> mergeMap(Map<String, Object> oldMap, Map<String, Object> newMap) {
        Map<String, Object> result = oldMap != null ? new HashMap<>(oldMap) : new HashMap<>();
        if (CollUtil.isNotEmpty(newMap)) {
            result.putAll(newMap);
        }
        return result;
    }

    @Getter
    private static class ShadowUpdateRejectedException extends RuntimeException {

        private final Long version;
        private final IotDeviceShadowErrorCodeEnum errorCode;

        private ShadowUpdateRejectedException(Long version, IotDeviceShadowErrorCodeEnum errorCode) {
            super(errorCode.getMessage());
            this.version = version;
            this.errorCode = errorCode;
        }

    }

    @Getter
    private static class ShadowCasExhaustedException extends RuntimeException {

        private final Long lastKnownVersion;
        private final boolean shadowNotFound;

        private ShadowCasExhaustedException(Long lastKnownVersion, boolean shadowNotFound) {
            this.lastKnownVersion = lastKnownVersion != null ? lastKnownVersion : 0L;
            this.shadowNotFound = shadowNotFound;
        }

        static ShadowCasExhaustedException shadowNotFound() {
            return new ShadowCasExhaustedException(0L, true);
        }

        static ShadowCasExhaustedException versionConflict(Long lastKnownVersion) {
            return new ShadowCasExhaustedException(lastKnownVersion, false);
        }

    }

}
