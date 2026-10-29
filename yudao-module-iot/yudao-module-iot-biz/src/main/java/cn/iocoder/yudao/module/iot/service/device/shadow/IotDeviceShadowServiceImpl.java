package cn.iocoder.yudao.module.iot.service.device.shadow;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.shadow.IotDeviceShadowErrorCodeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.shadow.IotDeviceShadowUpdateReqDTO;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceShadowProtocolUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceShadowDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceShadowMapper;
import cn.iocoder.yudao.module.iot.dal.redis.device.DeviceShadowRedisDAO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.SUCCESS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.core.util.IotDeviceShadowProtocolUtils.INNER_METHOD_GET;
import static cn.iocoder.yudao.module.iot.core.util.IotDeviceShadowProtocolUtils.INNER_METHOD_UPDATE;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 设备影子 Service 实现类
 * <p>
 * DB 为准：写入、版本校验、开关判断都读 DB；Redis 仅用于管理后台查询的读加速
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotDeviceShadowServiceImpl implements IotDeviceShadowService {

    /**
     * desired、reported 各自的最大属性数
     */
    private static final int MAX_PROPERTY_COUNT = 128;
    /**
     * 乐观锁冲突时的最大尝试次数
     */
    private static final int OPTIMISTIC_LOCK_RETRY = 3;

    @Resource
    private IotDeviceShadowMapper deviceShadowMapper;
    @Resource
    private DeviceShadowRedisDAO deviceShadowRedisDAO;

    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotDevicePropertyService devicePropertyService;
    @Resource
    @Lazy // 延迟加载，解决循环依赖
    private IotDeviceMessageService deviceMessageService;

    @Override
    public IotDeviceShadowDO getOrCreateShadow(IotDeviceDO device) {
        // 1. 优先读缓存：仅用于展示，允许在缓存过期前读到旧影子
        IotDeviceShadowDO shadow = deviceShadowRedisDAO.get(device.getId());
        if (shadow != null) {
            return shadow;
        }
        // 2. 读 DB（不存在则创建），并回填缓存
        shadow = getOrCreateDeviceShadowFromDB(device, CommonStatusEnum.ENABLE.getStatus());
        deviceShadowRedisDAO.set(shadow);
        return shadow;
    }

    @Override
    public void handleDeviceShadowUpdate(IotDeviceDO device, IotDeviceMessage message) {
        try {
            handleDeviceShadowUpdate0(device, message);
        } catch (Exception ex) {
            log.error("[handleDeviceShadowUpdate][device({}) message({}) 处理异常]", device.getId(), message, ex);
            sendShadowErrorReply(device, message, IotDeviceShadowErrorCodeEnum.SERVER_ERROR, 0L);
        }
    }

    /**
     * 处理设备影子上行：解析 params，并按内层 method 分发到 get、update
     * <p>
     * 影子未启用时，直接忽略，不回复
     *
     * @param device  设备
     * @param message 上行消息
     */
    private void handleDeviceShadowUpdate0(IotDeviceDO device, IotDeviceMessage message) {
        // 1.1 解析请求：params 结构为 { method, state: { reported }, version }
        IotDeviceShadowUpdateReqDTO reqDTO = JsonUtils.convertObject(message.getParams(),
                IotDeviceShadowUpdateReqDTO.class);
        if (reqDTO == null) {
            sendShadowErrorReply(device, message, IotDeviceShadowErrorCodeEnum.JSON_FORMAT_ERROR, 0L);
            return;
        }
        // 1.2 校验影子已启用：读 DB，避免缓存中的旧开关
        IotDeviceShadowDO shadow = getOrCreateDeviceShadowFromDB(device, CommonStatusEnum.ENABLE.getStatus());
        if (CommonStatusEnum.isDisable(shadow.getStatus())) {
            log.info("[handleDeviceShadowUpdate0][device({}) 影子未启用，忽略上行]", device.getId());
            return;
        }

        // 2. 内层 method 分发
        if (INNER_METHOD_GET.equals(reqDTO.getMethod())) {
            sendShadowGetReply(device, message.getServerId(), message.getRequestId(), shadow);
            return;
        }
        if (INNER_METHOD_UPDATE.equals(reqDTO.getMethod())) {
            handleReportedUpdate(device, message, reqDTO, shadow);
            return;
        }
        sendShadowErrorReply(device, message, StrUtil.isEmpty(reqDTO.getMethod())
                ? IotDeviceShadowErrorCodeEnum.METHOD_MISSING : IotDeviceShadowErrorCodeEnum.INVALID_METHOD,
                shadow.getVersion());
    }

    /**
     * 处理设备上报的 update：version 为 -1 时清空影子，否则合并 reported，并回复新版本
     *
     * @param device  设备
     * @param message 上行消息
     * @param reqDTO  上行 params
     * @param shadow  DB 中的当前影子，用于错误回复时携带版本
     */
    private void handleReportedUpdate(IotDeviceDO device, IotDeviceMessage message,
                                      IotDeviceShadowUpdateReqDTO reqDTO, IotDeviceShadowDO shadow) {
        // 1. version 为 -1 时，清空影子，无需 state
        if (Objects.equals(reqDTO.getVersion(), -1L)) {
            if (!resetDeviceShadow0(device.getId())) {
                sendShadowVersionConflictReply(device, message);
                return;
            }
            sendShadowReply(device, message.getServerId(), message.getRequestId(), SUCCESS.getCode(), SUCCESS.getMsg(),
                    IotDeviceShadowProtocolUtils.buildUpdateData(0L, DateUtil.currentSeconds()));
            return;
        }

        // 2. 校验 reported
        IotDeviceShadowUpdateReqDTO.ShadowState state = reqDTO.getState();
        if (state == null) {
            sendShadowErrorReply(device, message, IotDeviceShadowErrorCodeEnum.STATE_MISSING, shadow.getVersion());
            return;
        }
        if (state.getReported() == null) {
            sendShadowErrorReply(device, message, IotDeviceShadowErrorCodeEnum.REPORTED_MISSING, shadow.getVersion());
            return;
        }
        if (state.getReported().isEmpty()) {
            sendShadowErrorReply(device, message, IotDeviceShadowErrorCodeEnum.REPORTED_EMPTY, shadow.getVersion());
            return;
        }

        // 3. 合并更新 reported
        IotDeviceShadowDO newShadow;
        try {
            newShadow = updateDeviceShadowState(device.getId(), false, state.getReported(), reqDTO.getVersion());
        } catch (ServiceException ex) { // 合并后的属性数超过限制
            sendShadowErrorReply(device, message, IotDeviceShadowErrorCodeEnum.PROPERTY_COUNT_EXCEEDED,
                    shadow.getVersion());
            return;
        }
        if (newShadow == null) {
            sendShadowVersionConflictReply(device, message);
            return;
        }
        sendShadowReply(device, message.getServerId(), message.getRequestId(), SUCCESS.getCode(), SUCCESS.getMsg(),
                IotDeviceShadowProtocolUtils.buildUpdateData(newShadow.getVersion(), newShadow.getShadowTime()));
    }

    @Override
    public void updateDesiredState(Long deviceId, Map<String, Object> desired) {
        // 1. 校验影子已启用：读 DB，避免缓存中的旧开关
        IotDeviceDO device = deviceService.validateDeviceExists(deviceId);
        IotDeviceShadowDO shadow = getOrCreateDeviceShadowFromDB(device, CommonStatusEnum.ENABLE.getStatus());
        if (CommonStatusEnum.isDisable(shadow.getStatus())) {
            throw exception(DEVICE_SHADOW_NOT_ENABLED);
        }
        if (CollUtil.isEmpty(desired)) {
            return;
        }

        // 2. 合并更新 desired
        IotDeviceShadowDO newShadow = updateDeviceShadowState(deviceId, true, desired, null);
        if (newShadow == null) {
            throw exception(DEVICE_SHADOW_VERSION_CONFLICT);
        }

        // 3. 设备在线时，主动推送全量影子
        String serverId = devicePropertyService.getDeviceServerId(deviceId);
        if (StrUtil.isNotEmpty(serverId)) {
            sendShadowGetReply(device, serverId, null, newShadow);
        }
    }

    @Override
    public void resetShadow(Long deviceId) {
        deviceService.validateDeviceExists(deviceId);
        if (!resetDeviceShadow0(deviceId)) {
            throw exception(DEVICE_SHADOW_VERSION_CONFLICT);
        }
    }

    @Override
    public void updateStatus(Long deviceId, Integer status) {
        // 1. 获得影子：不存在时，直接按目标状态创建
        IotDeviceDO device = deviceService.validateDeviceExists(deviceId);
        IotDeviceShadowDO shadow = getOrCreateDeviceShadowFromDB(device, status);

        // 2. 更新状态
        if (ObjUtil.notEqual(shadow.getStatus(), status)) {
            deviceShadowMapper.updateByDeviceId(new IotDeviceShadowDO().setStatus(status), deviceId);
        }
        deleteDeviceShadowCache(deviceId);
    }

    @Override
    public void deleteByDeviceId(Long deviceId) {
        deviceShadowMapper.deleteByDeviceId(deviceId);
        deleteDeviceShadowCache(deviceId);
    }

    /**
     * 从 DB 获得影子，不存在时创建
     *
     * @param device 设备
     * @param status 创建时的状态
     * @return 影子
     */
    private IotDeviceShadowDO getOrCreateDeviceShadowFromDB(IotDeviceDO device, Integer status) {
        IotDeviceShadowDO shadow = deviceShadowMapper.selectByDeviceId(device.getId());
        if (shadow != null) {
            return shadow;
        }
        // lazy 创建：首次访问时，才插入空影子；并发插入时，以先插入的为准
        shadow = new IotDeviceShadowDO().setDeviceId(device.getId()).setProductId(device.getProductId())
                .setProductKey(device.getProductKey()).setDeviceName(device.getDeviceName())
                .setDesired(new HashMap<>()).setReported(new HashMap<>()).setMetadata(new HashMap<>())
                .setVersion(0L).setShadowTime(DateUtil.currentSeconds()).setStatus(status);
        try {
            deviceShadowMapper.insert(shadow);
            return shadow;
        } catch (DuplicateKeyException ex) {
            return deviceShadowMapper.selectByDeviceId(device.getId());
        }
    }

    /**
     * 乐观锁合并更新 desired 或 reported
     * <p>
     * 每次尝试都读 DB 最新影子重新合并，避免丢失并发更新
     *
     * @param deviceId      设备编号
     * @param desired       true 更新 desired；false 更新 reported
     * @param values        待合并的属性
     * @param expectVersion 期望的当前版本；null 表示不校验
     * @return 更新后的影子；版本冲突时，返回 null
     */
    private IotDeviceShadowDO updateDeviceShadowState(Long deviceId, boolean desired, Map<String, Object> values,
                                                      Long expectVersion) {
        for (int i = 0; i < OPTIMISTIC_LOCK_RETRY; i++) {
            // 1. 校验版本
            IotDeviceShadowDO latest = deviceShadowMapper.selectByDeviceId(deviceId);
            Assert.notNull(latest, "设备影子({})不存在", deviceId);
            if (expectVersion != null && ObjUtil.notEqual(expectVersion, latest.getVersion())) {
                return null;
            }

            // 2.1 合并属性，并校验属性数
            Map<String, Object> state = new HashMap<>(MapUtil.emptyIfNull(desired ? latest.getDesired() : latest.getReported()));
            state.putAll(values);
            if (state.size() > MAX_PROPERTY_COUNT) {
                throw exception(DEVICE_SHADOW_PROPERTY_COUNT_EXCEEDED);
            }
            // 2.2 记录属性的更新时间
            long now = DateUtil.currentSeconds();
            Map<String, Object> metadata = new HashMap<>(MapUtil.emptyIfNull(latest.getMetadata()));
            if (desired) {
                IotDeviceShadowProtocolUtils.putDesiredTimestamps(metadata, values.keySet(), now);
            } else {
                IotDeviceShadowProtocolUtils.putReportedTimestamps(metadata, values.keySet(), now);
            }

            // 3. 按 version 更新：成功则返回；失败则重试
            IotDeviceShadowDO update = new IotDeviceShadowDO()
                    .setDesired(desired ? state : latest.getDesired())
                    .setReported(desired ? latest.getReported() : state)
                    .setMetadata(metadata).setVersion(latest.getVersion() + 1).setShadowTime(now);
            if (deviceShadowMapper.updateByDeviceIdAndVersion(update, deviceId, latest.getVersion()) > 0) {
                deleteDeviceShadowCache(deviceId);
                return update;
            }
        }
        return null;
    }

    /**
     * 乐观锁清空影子：desired、reported、metadata 置空，version 归零
     *
     * @param deviceId 设备编号
     * @return 是否成功；影子不存在时，无需清空，也返回 true；版本冲突重试耗尽时，返回 false
     */
    private boolean resetDeviceShadow0(Long deviceId) {
        for (int i = 0; i < OPTIMISTIC_LOCK_RETRY; i++) {
            IotDeviceShadowDO latest = deviceShadowMapper.selectByDeviceId(deviceId);
            if (latest == null) {
                return true;
            }
            IotDeviceShadowDO update = new IotDeviceShadowDO()
                    .setDesired(new HashMap<>()).setReported(new HashMap<>()).setMetadata(new HashMap<>())
                    .setVersion(0L).setShadowTime(DateUtil.currentSeconds());
            if (deviceShadowMapper.updateByDeviceIdAndVersion(update, deviceId, latest.getVersion()) > 0) {
                deleteDeviceShadowCache(deviceId);
                return true;
            }
        }
        return false;
    }

    /**
     * 删除影子缓存：存在事务时，在提交后删除，避免并发查询回填提交前的旧影子
     *
     * @param deviceId 设备编号
     */
    private void deleteDeviceShadowCache(Long deviceId) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deviceShadowRedisDAO.delete(deviceId);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                deviceShadowRedisDAO.delete(deviceId);
            }

        });
    }

    /**
     * 回复全量影子：设备 get 请求的回复，或 desired 更新后的主动推送（requestId 为空）
     */
    private void sendShadowGetReply(IotDeviceDO device, String serverId, String requestId, IotDeviceShadowDO shadow) {
        sendShadowReply(device, serverId, requestId, SUCCESS.getCode(), SUCCESS.getMsg(),
                IotDeviceShadowProtocolUtils.buildGetData(shadow.getDesired(), shadow.getReported(),
                        shadow.getVersion(), shadow.getShadowTime()));
    }

    /**
     * 回复版本冲突：带上 DB 当前版本，便于设备重新同步
     */
    private void sendShadowVersionConflictReply(IotDeviceDO device, IotDeviceMessage message) {
        IotDeviceShadowDO latest = deviceShadowMapper.selectByDeviceId(device.getId());
        sendShadowErrorReply(device, message, IotDeviceShadowErrorCodeEnum.VERSION_CONFLICT,
                latest != null ? latest.getVersion() : 0L);
    }

    /**
     * 回复错误：data 仍带 version，便于设备重新同步
     */
    private void sendShadowErrorReply(IotDeviceDO device, IotDeviceMessage message,
                                      IotDeviceShadowErrorCodeEnum errorCode, Long version) {
        sendShadowReply(device, message.getServerId(), message.getRequestId(),
                errorCode.getCode(), errorCode.getMessage(),
                IotDeviceShadowProtocolUtils.buildUpdateData(version, DateUtil.currentSeconds()));
    }

    /**
     * 下发影子回复（标准 IotDeviceMessage 封套）
     * <p>
     * method 使用 {@link IotDeviceMessageMethodEnum#SHADOW_UPDATE}，带 code 即为回复，
     * 网关下发到 thing/shadow/update_reply Topic；发送失败只打印日志，不影响上行处理
     */
    private void sendShadowReply(IotDeviceDO device, String serverId, String requestId,
                                 Integer code, String msg, Map<String, Object> data) {
        IotDeviceMessage reply = IotDeviceMessage.replyOf(requestId,
                IotDeviceMessageMethodEnum.SHADOW_UPDATE.getMethod(), data, code, msg);
        reply.setDeviceId(device.getId()).setTenantId(device.getTenantId()).setServerId(serverId);
        try {
            deviceMessageService.sendDeviceMessage(reply, device, serverId);
        } catch (Exception ex) {
            log.error("[sendShadowReply][device({}) 下发影子回复失败]", device.getId(), ex);
        }
    }

}
