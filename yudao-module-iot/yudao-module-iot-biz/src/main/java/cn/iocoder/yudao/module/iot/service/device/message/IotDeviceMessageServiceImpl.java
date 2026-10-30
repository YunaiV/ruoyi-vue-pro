package cn.iocoder.yudao.module.iot.service.device.message;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.message.IotDeviceMessagePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.statistics.vo.IotStatisticsDeviceMessageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.statistics.vo.IotStatisticsDeviceMessageSummaryByDateRespVO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.enums.device.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;
import cn.iocoder.yudao.module.iot.core.topic.event.IotDeviceEventPostReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertyPackPostReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertyPostReqDTO;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceMessageDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.storage.IotDeviceMessageDAO;
import cn.iocoder.yudao.module.iot.enums.product.IotProductDeviceTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import cn.iocoder.yudao.module.iot.service.device.shadow.IotDeviceShadowService;
import cn.iocoder.yudao.module.iot.service.ota.IotOtaTaskRecordService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import cn.iocoder.yudao.module.iot.service.rule.data.IotDataRuleService;
import cn.iocoder.yudao.module.iot.service.rule.scene.IotSceneRuleService;
import com.google.common.base.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.DEVICE_DOWNSTREAM_FAILED_SERVER_ID_NULL;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.DEVICE_MESSAGE_RAW_METHOD_RESERVED;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.DEVICE_MESSAGE_RAW_PAYLOAD_INVALID;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.DEVICE_MESSAGE_RAW_PROTOCOL_NOT_SUPPORTED;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.PRODUCT_NOT_EXISTS;

/**
 * IoT 设备消息 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotDeviceMessageServiceImpl implements IotDeviceMessageService {

    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotDevicePropertyService devicePropertyService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private IotOtaTaskRecordService otaTaskRecordService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private IotDeviceShadowService deviceShadowService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private IotProductService productService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private IotSceneRuleService sceneRuleService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private IotDataRuleService dataRuleService;

    @Resource
    private IotDeviceMessageDAO deviceMessageDAO;

    @Resource
    private IotDeviceMessageProducer deviceMessageProducer;

    @Async
    void createDeviceLogAsync(IotDeviceMessage message) {
        IotDeviceMessageDO messageDO = BeanUtils.toBean(message, IotDeviceMessageDO.class)
                .setUpstream(IotDeviceMessageUtils.isUpstreamMessage(message))
                .setReply(IotDeviceMessageUtils.isReplyMessage(message))
                .setIdentifier(IotDeviceMessageUtils.getIdentifier(message));
        if (message.getParams() != null) {
            messageDO.setParams(JsonUtils.toJsonString(messageDO.getParams()));
        }
        if (messageDO.getData() != null) {
            messageDO.setData(JsonUtils.toJsonString(messageDO.getData()));
        }
        if (StrUtil.isEmpty(messageDO.getId())) {
            messageDO.setId(IotDeviceMessageUtils.generateMessageId());
        }
        if (messageDO.getTs() == null) {
            messageDO.setTs(System.currentTimeMillis());
        }
        try {
            deviceMessageDAO.insert(messageDO);
        } catch (Exception ex) {
            // 特殊：@Async 方法的异常默认会被 handler 吞掉，这里显式记录便于排查
            log.error("[createDeviceLogAsync][消息日志写入失败 deviceId({}) messageId({}) paramsLen({}) dataLen({})]",
                    messageDO.getDeviceId(), messageDO.getId(),
                    StrUtil.length((String) messageDO.getParams()),
                    StrUtil.length((String) messageDO.getData()), ex);
        }
    }

    @Override
    public IotDeviceMessage sendDeviceMessage(IotDeviceMessage message) {
        IotDeviceDO device = deviceService.validateDeviceExists(message.getDeviceId());
        return sendDeviceMessage(message, device);
    }

    @Override
    public IotDeviceMessage sendDeviceMessage(IotDeviceMessage message, IotDeviceDO device) {
        return sendDeviceMessage(message, device, null);
    }

    @Override
    public IotDeviceMessage sendDeviceMessage(IotDeviceMessage message, IotDeviceDO device, String serverId) {
        return sendDeviceMessage(message, device, serverId, null);
    }

    @Override
    public IotDeviceMessage sendDeviceMessage(IotDeviceMessage message, IotDeviceDO device, String serverId,
                                              LocalDateTime eventTime) {
        // 0. 透传：raw.post 只能由网关创建；raw.push 校验后改写为 raw，网关从 raw 取字节；其它消息不能携带 raw
        byte[] rawPayload = validateRawMessage(message, device);
        message.setRaw(rawPayload != null ? IotDeviceMessageUtils.encodeRaw(rawPayload) : null);
        if (rawPayload != null) {
            message.setParams(null);
        }
        // 1. 补充信息
        appendDeviceMessage(message, device, eventTime);

        // 2.1 情况一：发送上行消息
        boolean upstream = IotDeviceMessageUtils.isUpstreamMessage(message);
        if (upstream) {
            deviceMessageProducer.sendDeviceMessage(message);
            return message;
        }

        // 2.2 情况二：发送下行消息
        // 如果是下行消息，需要校验 serverId 存在
        // TODO 芋艿：【设计】下行消息需要区分 PUSH 和 PULL 模型
        // 1. PUSH 模型：适用于 MQTT 等长连接协议。通过 serverId 将消息路由到指定网关，实时推送。
        // 2. PULL 模型：适用于 HTTP 等短连接协议。设备无固定 serverId，无法主动推送。
        // 解决方案：
        // 当 serverId 不存在时，将下行消息存入“待拉取消息表”（例如 iot_device_pull_message）。
        // 设备端通过定时轮询一个新增的 API（例如 /iot/message/pull）来拉取属于自己的消息。
        if (StrUtil.isEmpty(serverId)) {
            serverId = devicePropertyService.getDeviceServerId(device.getId());
            if (StrUtil.isEmpty(serverId)) {
                throw exception(DEVICE_DOWNSTREAM_FAILED_SERVER_ID_NULL);
            }
        }
        deviceMessageProducer.sendDeviceMessageToGateway(serverId, message);
        // 特殊：记录消息日志。原因：上行消息，消费时，已经会记录；下行消息，因为消费在 Gateway 端，所以需要在这里记录
        getSelf().createDeviceLogAsync(message);
        return message;
    }

    /**
     * 校验透传消息：raw.post 只能由网关创建；raw.push 校验 params.payload 与产品协议
     *
     * @param message 消息
     * @param device  设备信息
     * @return raw.push 请求的载荷；其它消息返回 null
     */
    private byte[] validateRawMessage(IotDeviceMessage message, IotDeviceDO device) {
        // 1.1 raw.post 只能由网关创建
        if (IotDeviceMessageMethodEnum.RAW_POST.getMethod().equals(message.getMethod())) {
            throw exception(DEVICE_MESSAGE_RAW_METHOD_RESERVED);
        }
        // 1.2 只校验 raw.push 请求，其它消息直接跳过
        if (!Objects.equal(message.getMethod(), IotDeviceMessageMethodEnum.RAW_PUSH.getMethod())
                || IotDeviceMessageUtils.isReplyMessage(message)) {
            return null;
        }

        // 2. 校验产品协议：HTTP、CoAP 没有长连接，不支持透传下发
        IotProductDO product = productService.getProductFromCache(device.getProductId());
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        IotProtocolTypeEnum protocol = IotProtocolTypeEnum.of(product.getProtocolType());
        if (protocol == IotProtocolTypeEnum.HTTP || protocol == IotProtocolTypeEnum.COAP) {
            throw exception(DEVICE_MESSAGE_RAW_PROTOCOL_NOT_SUPPORTED, product.getProtocolType());
        }

        // 3. 校验载荷：按产品协议的字节规则，与网关 encode 输出、脚本调试是同一份
        try {
            byte[] payload = IotDeviceMessageUtils.decodeRawPayload(message.getParams(), IotDeviceMessageUtils.RAW_PAYLOAD_MAX_BYTES);
            IotDeviceMessageUtils.validateRawPayload(protocol, payload);
            return payload;
        } catch (IllegalArgumentException e) {
            throw exception(DEVICE_MESSAGE_RAW_PAYLOAD_INVALID, e.getMessage());
        }
    }

    /**
     * 补充消息的后端字段
     *
     * @param message   消息
     * @param device    设备信息
     * @param eventTime 事件时间；为空时使用当前时间
     */
    private void appendDeviceMessage(IotDeviceMessage message, IotDeviceDO device, LocalDateTime eventTime) {
        message.setId(IotDeviceMessageUtils.generateMessageId())
                .setReportTime(eventTime != null ? eventTime : LocalDateTime.now())
                .setDeviceId(device.getId()).setTenantId(device.getTenantId());
        // 特殊：如果设备没有指定 requestId，则使用 messageId
        if (StrUtil.isEmpty(message.getRequestId())) {
            message.setRequestId(message.getId());
        }
    }

    @Override
    public void handleUpstreamDeviceMessage(IotDeviceMessage message, IotDeviceDO device) {
        // 1. 处理消息
        Object replyData = null;
        ServiceException serviceException = null;
        try {
            replyData = handleUpstreamDeviceMessage0(message, device);
        } catch (ServiceException ex) {
            serviceException = ex;
            log.warn("[handleUpstreamDeviceMessage][message({}) 业务异常]", message, serviceException);
        } catch (Exception ex) {
            log.error("[handleUpstreamDeviceMessage][message({}) 发生异常]", message, ex);
            throw ex;
        }

        // 2. 记录消息
        getSelf().createDeviceLogAsync(message);

        // 3. 回复消息。前提：非 _reply 消息、非禁用回复的消息
        //    另外：带原始帧（raw）的是网关透传解析出的消息，不自动回复；设备经标准路径上报的 raw 由网关清空，不能伪造
        if (IotDeviceMessageUtils.isReplyMessage(message)
                || IotDeviceMessageMethodEnum.isReplyDisabled(message.getMethod())
                || message.getRaw() != null
                || StrUtil.isEmpty(message.getServerId())) {
            return;
        }
        try {
            IotDeviceMessage replyMessage = IotDeviceMessage.replyOf(message.getRequestId(), message.getMethod(), replyData,
                    serviceException != null ? serviceException.getCode() : null,
                    serviceException != null ? serviceException.getMessage() : null);
            sendDeviceMessage(replyMessage, device, message.getServerId());
        } catch (Exception ex) {
            log.error("[handleUpstreamDeviceMessage][message({}) 回复消息失败]", message, ex);
        }
    }

    private Object handleUpstreamDeviceMessage0(IotDeviceMessage message, IotDeviceDO device) {
        // 设备上下线
        if (Objects.equal(message.getMethod(), IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod())) {
            processStateUpdateMessage(message, device);
            return null;
        }

        // 属性上报
        if (Objects.equal(message.getMethod(), IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod())) {
            devicePropertyService.saveDeviceProperty(device, message);
            return null;
        }
        // 批量上报（属性+事件+子设备）
        if (Objects.equal(message.getMethod(), IotDeviceMessageMethodEnum.PROPERTY_PACK_POST.getMethod())) {
            handlePackMessage(message, device);
            return null;
        }

        // OTA 上报升级进度
        if (Objects.equal(message.getMethod(), IotDeviceMessageMethodEnum.OTA_PROGRESS.getMethod())) {
            otaTaskRecordService.updateOtaRecordProgress(device, message);
            return null;
        }

        // 设备影子更新
        if (Objects.equal(message.getMethod(), IotDeviceMessageMethodEnum.SHADOW_UPDATE.getMethod())) {
            deviceShadowService.handleDeviceShadowUpdate(device, message);
            return null;
        }

        // 添加拓扑关系
        if (Objects.equal(message.getMethod(), IotDeviceMessageMethodEnum.TOPO_ADD.getMethod())) {
            return deviceService.handleTopoAddMessage(message, device);
        }
        // 删除拓扑关系
        if (Objects.equal(message.getMethod(), IotDeviceMessageMethodEnum.TOPO_DELETE.getMethod())) {
            return deviceService.handleTopoDeleteMessage(message, device);
        }
        // 获取拓扑关系
        if (Objects.equal(message.getMethod(), IotDeviceMessageMethodEnum.TOPO_GET.getMethod())) {
            return deviceService.handleTopoGetMessage(device);
        }

        // 子设备动态注册
        if (Objects.equal(message.getMethod(), IotDeviceMessageMethodEnum.SUB_DEVICE_REGISTER.getMethod())) {
            return deviceService.handleSubDeviceRegisterMessage(message, device);
        }

        return null;
    }

    // ========== 设备状态处理方法 ==========

    /**
     * 处理设备状态消息
     *
     * 1. 校验状态值，忽略旧网关节点的离线
     * 2. 在状态事务中：条件更新状态；状态真的变化时，执行场景联动、数据流转
     * 3. 状态事务提交后：网关离线时，联动子设备离线
     *
     * @param message 消息
     * @param device  设备
     */
    private void processStateUpdateMessage(IotDeviceMessage message, IotDeviceDO device) {
        // 1.1 校验状态值：只接受在线、离线（设备自己上报的状态，同样在这里校验）
        String stateStr = IotDeviceMessageUtils.getIdentifier(message);
        Integer state = NumberUtil.isInteger(stateStr) ? Integer.valueOf(stateStr) : null;
        if (ObjUtil.notEqual(state, IotDeviceStateEnum.ONLINE.getState())
                && ObjUtil.notEqual(state, IotDeviceStateEnum.OFFLINE.getState())) {
            throw invalidParamException("设备状态({}) 只能是在线或离线", stateStr);
        }
        // 1.2 旧网关节点的离线，不覆盖新节点上的连接
        if (isStaleServerOffline(message, device, state)) {
            return;
        }
        // 1.3 拒绝已有事务：REQUIRED 会加入外层事务，事务方法返回时并未提交，后续的子设备联动就失去了前提
        // 说明：Local 总线在发布线程上同步消费，所以发布方不能在事务内发布状态消息
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            log.error("[processStateUpdateMessage][message({}) 状态消息不能在已有事务中处理]", message);
            throw new IllegalStateException("状态消息不能在已有事务中处理");
        }

        // 2. 状态事务：经过代理开启事务，返回时已经提交
        LocalDateTime time = message.getReportTime() != null ? message.getReportTime() : LocalDateTime.now();
        boolean changed = getSelf().handleStateUpdateMessage(message, device, state, time);

        // 3. 网关离线：提交后，再联动子设备离线。每个子设备走自己的消息和事务，不加入网关的状态事务
        if (changed && Objects.equal(state, IotDeviceStateEnum.OFFLINE.getState())
                && IotProductDeviceTypeEnum.isGateway(device.getDeviceType())) {
            deviceService.handleGatewayOffline(device, time);
        }
    }

    /**
     * 在状态事务中处理设备状态：条件更新状态；状态真的变化时，执行场景联动、数据流转
     *
     * 为什么规则也放在事务内？保证“已提交的有效状态变化，提交前已经尝试过规则”：
     * 规则执行完成前失败时，状态一起回滚，异常抛给消息总线，重投后再次处理
     *
     * 注意：必须经过 Spring 代理调用，且调用方不在事务中；这样方法返回时，事务已经提交
     *
     * @param message 消息
     * @param device  设备
     * @param state   状态
     * @param time    事件时间
     * @return 状态是否真的发生了变化
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean handleStateUpdateMessage(IotDeviceMessage message, IotDeviceDO device, Integer state, LocalDateTime time) {
        // 1. 条件更新状态：重复的、迟到的状态，直接返回，不触发规则
        // 带 serverId 的上线来自网关的连接，状态未变化时也刷新最后上线时间
        if (!deviceService.updateDeviceState(device, state, time, StrUtil.isNotEmpty(message.getServerId()))) {
            return false;
        }

        // 2. 执行场景联动、数据流转：两个规则订阅者跳过状态消息，统一在这里触发
        sceneRuleService.executeSceneRuleByDevice(message);
        dataRuleService.executeDataRule(message);

        // 3. 规则可能把事务标记为回滚：本地 rollback-only 时，提交会静默回滚、不抛异常，所以这里显式失败
        if (TransactionAspectSupport.currentTransactionStatus().isRollbackOnly()) {
            throw new IllegalStateException("状态事务已被标记回滚");
        }
        return true;
    }

    /**
     * 判断是否是旧网关节点的离线：设备已经连接到其它网关节点时，原节点上旧连接的离线不生效
     *
     * 只比较“离线且带 serverId”的网关连接事件：平台、子设备联动、离线检测发出的离线，serverId 为空，照常生效；
     * EMQX 的 serverId 是接收 webhook 的节点，不代表连接归属，所以不比较
     *
     * @param message 消息
     * @param device  设备
     * @param state   状态
     * @return 是否是旧网关节点的离线
     */
    private boolean isStaleServerOffline(IotDeviceMessage message, IotDeviceDO device, Integer state) {
        if (ObjUtil.notEqual(state, IotDeviceStateEnum.OFFLINE.getState())
                || StrUtil.isEmpty(message.getServerId()) || isEmqxDevice(device)) {
            return false;
        }
        String currentServerId = devicePropertyService.getDeviceServerId(device.getId());
        if (currentServerId == null || currentServerId.equals(message.getServerId())) {
            return false;
        }
        log.info("[isStaleServerOffline][设备({}) 当前连接在网关({})，忽略网关({}) 的离线]",
                device.getId(), currentServerId, message.getServerId());
        return true;
    }

    /**
     * 是否是 EMQX 协议的设备：产品不存在时，按非 EMQX 处理
     *
     * @param device 设备
     * @return 是否是 EMQX 协议的设备
     */
    private boolean isEmqxDevice(IotDeviceDO device) {
        IotProductDO product = productService.getProductFromCache(device.getProductId());
        return product != null && IotProtocolTypeEnum.of(product.getProtocolType()) == IotProtocolTypeEnum.EMQX;
    }

    // ========== 批量上报处理方法 ==========

    /**
     * 处理批量上报消息
     * <p>
     * 将 pack 消息拆分成多条标准消息，发送到 MQ 让规则引擎处理
     *
     * @param packMessage   批量消息
     * @param gatewayDevice 网关设备
     */
    private void handlePackMessage(IotDeviceMessage packMessage, IotDeviceDO gatewayDevice) {
        // 1. 解析参数
        IotDevicePropertyPackPostReqDTO params = JsonUtils.convertObject(
                packMessage.getParams(), IotDevicePropertyPackPostReqDTO.class);
        if (params == null) {
            log.warn("[handlePackMessage][消息({}) 参数解析失败]", packMessage);
            return;
        }

        // 2. 处理网关设备（自身）的数据
        // 特殊：拆分出的消息沿用 pack 的上报时间，避免迟到的 pack 变成“新”的消息（例如说，强制上线用的是它的时间）
        LocalDateTime reportTime = packMessage.getReportTime() != null ? packMessage.getReportTime() : LocalDateTime.now();
        sendDevicePackData(gatewayDevice, packMessage.getServerId(), params.getProperties(), params.getEvents(), reportTime);

        // 3. 处理子设备的数据
        if (CollUtil.isEmpty(params.getSubDevices())) {
            return;
        }
        for (IotDevicePropertyPackPostReqDTO.SubDeviceData subDeviceData : params.getSubDevices()) {
            try {
                IotDeviceIdentity identity = subDeviceData.getIdentity();
                IotDeviceDO subDevice = deviceService.getDeviceFromCache(identity.getProductKey(), identity.getDeviceName());
                if (subDevice == null) {
                    log.warn("[handlePackMessage][子设备({}/{}) 不存在]", identity.getProductKey(), identity.getDeviceName());
                    continue;
                }
                // 特殊：子设备不需要指定 serverId，因为子设备实际可能连接在不同的 gateway-server 上，导致 serverId 不同
                sendDevicePackData(subDevice, null, subDeviceData.getProperties(), subDeviceData.getEvents(),
                        reportTime);
            } catch (Exception ex) {
                log.error("[handlePackMessage][子设备({}/{}) 数据处理失败]", subDeviceData.getIdentity().getProductKey(),
                        subDeviceData.getIdentity().getDeviceName(), ex);
            }
        }
    }

    /**
     * 发送设备 pack 数据到 MQ（属性 + 事件）
     *
     * @param device     设备
     * @param serverId   服务标识
     * @param properties 属性数据
     * @param events     事件数据
     * @param reportTime 上报时间，沿用 pack 消息的上报时间
     */
    private void sendDevicePackData(IotDeviceDO device, String serverId,
                                    Map<String, Object> properties,
                                    Map<String, IotDevicePropertyPackPostReqDTO.EventValue> events,
                                    LocalDateTime reportTime) {
        // 1. 发送属性消息
        if (MapUtil.isNotEmpty(properties)) {
            IotDeviceMessage propertyMsg = IotDeviceMessage.requestOf(
                    device.getId(), device.getTenantId(), serverId,
                    IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(),
                    IotDevicePropertyPostReqDTO.of(properties));
            propertyMsg.setReportTime(reportTime);
            deviceMessageProducer.sendDeviceMessage(propertyMsg);
        }

        // 2. 发送事件消息
        if (MapUtil.isNotEmpty(events)) {
            for (Map.Entry<String, IotDevicePropertyPackPostReqDTO.EventValue> eventEntry : events.entrySet()) {
                String eventId = eventEntry.getKey();
                IotDevicePropertyPackPostReqDTO.EventValue eventValue = eventEntry.getValue();
                if (eventValue == null) {
                    continue;
                }
                IotDeviceMessage eventMsg = IotDeviceMessage.requestOf(
                        device.getId(), device.getTenantId(), serverId,
                        IotDeviceMessageMethodEnum.EVENT_POST.getMethod(),
                        IotDeviceEventPostReqDTO.of(eventId, eventValue.getValue(), eventValue.getTime()));
                eventMsg.setReportTime(reportTime);
                deviceMessageProducer.sendDeviceMessage(eventMsg);
            }
        }
    }

    // ========= 设备消息查询 ==========

    @Override
    public PageResult<IotDeviceMessageDO> getDeviceMessagePage(IotDeviceMessagePageReqVO pageReqVO) {
        // 先校验设备属于当前租户（MySQL 租户插件生效）；时序库查询本身只按 deviceId 限定
        deviceService.validateDeviceExists(pageReqVO.getDeviceId());
        return deviceMessageDAO.selectPage(pageReqVO);
    }

    @Override
    public List<IotDeviceMessageDO> getDeviceMessageListByRequestIdsAndReply(Long deviceId, List<String> requestIds, Boolean reply) {
        if (CollUtil.isEmpty(requestIds)) {
            return ListUtil.of();
        }
        return deviceMessageDAO.selectListByRequestIdsAndReply(deviceId, requestIds, reply);
    }

    @Override
    public Long getDeviceMessageCount(LocalDateTime createTime) {
        return deviceMessageDAO.selectCountByCreateTime(
                createTime != null ? LocalDateTimeUtil.toEpochMilli(createTime) : null);
    }

    @Override
    public List<IotStatisticsDeviceMessageSummaryByDateRespVO> getDeviceMessageSummaryByDate(
            IotStatisticsDeviceMessageReqVO reqVO) {
        // 1. 按小时统计，获取分项统计数据
        List<Map<String, Object>> countList = deviceMessageDAO.selectDeviceMessageCountGroupByDate(
                LocalDateTimeUtil.toEpochMilli(reqVO.getTimes()[0]),
                LocalDateTimeUtil.toEpochMilli(reqVO.getTimes()[1]));

        // 2. 按照日期间隔，合并数据
        List<LocalDateTime[]> timeRanges = LocalDateTimeUtils.getDateRangeList(reqVO.getTimes()[0], reqVO.getTimes()[1],
                reqVO.getInterval());
        return convertList(timeRanges, times -> {
            Integer upstreamCount = countList.stream()
                    .filter(vo -> LocalDateTimeUtils.isBetween(times[0], times[1], (Timestamp) vo.get("time")))
                    .mapToInt(value -> MapUtil.getInt(value, "upstream_count")).sum();
            Integer downstreamCount = countList.stream()
                    .filter(vo -> LocalDateTimeUtils.isBetween(times[0], times[1], (Timestamp) vo.get("time")))
                    .mapToInt(value -> MapUtil.getInt(value, "downstream_count")).sum();
            return new IotStatisticsDeviceMessageSummaryByDateRespVO()
                    .setTime(LocalDateTimeUtils.formatDateRange(times[0], times[1], reqVO.getInterval()))
                    .setUpstreamCount(upstreamCount).setDownstreamCount(downstreamCount);
        });
    }

    private IotDeviceMessageServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
