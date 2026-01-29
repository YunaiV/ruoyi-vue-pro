package cn.iocoder.yudao.module.iot.service.device.message;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
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
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;
import cn.iocoder.yudao.module.iot.core.topic.event.IotDeviceEventPostReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertyPackPostReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertyPostReqDTO;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceMessageDO;
import cn.iocoder.yudao.module.iot.dal.tdengine.IotDeviceMessageMapper;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import cn.iocoder.yudao.module.iot.service.ota.IotOtaTaskRecordService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.DEVICE_DOWNSTREAM_FAILED_SERVER_ID_NULL;

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
    private IotDeviceMessageMapper deviceMessageMapper;

    @Resource
    private IotDeviceMessageProducer deviceMessageProducer;

    @Override
    public void defineDeviceMessageStable() {
        if (StrUtil.isNotEmpty(deviceMessageMapper.showSTable())) {
            log.info("[defineDeviceMessageStable][设备消息超级表已存在，创建跳过]");
            return;
        }
        log.info("[defineDeviceMessageStable][设备消息超级表不存在，创建开始...]");
        deviceMessageMapper.createSTable();
        log.info("[defineDeviceMessageStable][设备消息超级表不存在，创建成功]");
    }

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
        deviceMessageMapper.insert(messageDO);
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

    private IotDeviceMessage sendDeviceMessage(IotDeviceMessage message, IotDeviceDO device, String serverId) {
        // 1. 补充信息
        appendDeviceMessage(message, device);

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
     * 补充消息的后端字段
     *
     * @param message 消息
     * @param device  设备信息
     */
    private void appendDeviceMessage(IotDeviceMessage message, IotDeviceDO device) {
        message.setId(IotDeviceMessageUtils.generateMessageId()).setReportTime(LocalDateTime.now())
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
        if (IotDeviceMessageUtils.isReplyMessage(message)
                || IotDeviceMessageMethodEnum.isReplyDisabled(message.getMethod())
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

    // TODO @芋艿：可优化：未来逻辑复杂后，可以独立拆除 Processor 处理器
    private Object handleUpstreamDeviceMessage0(IotDeviceMessage message, IotDeviceDO device) {
        // 设备上下线
        if (Objects.equal(message.getMethod(), IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod())) {
            String stateStr = IotDeviceMessageUtils.getIdentifier(message);
            assert stateStr != null;
            Assert.notEmpty(stateStr, "设备状态不能为空");
            Integer state = Integer.valueOf(stateStr);
            deviceService.updateDeviceState(device, state);
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
        sendDevicePackData(gatewayDevice, packMessage.getServerId(), params.getProperties(), params.getEvents());

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
                sendDevicePackData(subDevice, null, subDeviceData.getProperties(), subDeviceData.getEvents());
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
     */
    private void sendDevicePackData(IotDeviceDO device, String serverId,
                                    Map<String, Object> properties,
                                    Map<String, IotDevicePropertyPackPostReqDTO.EventValue> events) {
        // 1. 发送属性消息
        if (MapUtil.isNotEmpty(properties)) {
            IotDeviceMessage propertyMsg = IotDeviceMessage.requestOf(
                    device.getId(), device.getTenantId(), serverId,
                    IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(),
                    IotDevicePropertyPostReqDTO.of(properties));
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
                deviceMessageProducer.sendDeviceMessage(eventMsg);
            }
        }
    }

    // ========= 设备消息查询 ==========

    @Override
    public PageResult<IotDeviceMessageDO> getDeviceMessagePage(IotDeviceMessagePageReqVO pageReqVO) {
        try {
            IPage<IotDeviceMessageDO> page = deviceMessageMapper.selectPage(
                    new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize()), pageReqVO);
            return new PageResult<>(page.getRecords(), page.getTotal());
        } catch (Exception exception) {
            if (exception.getMessage().contains("Table does not exist")) {
                return PageResult.empty();
            }
            throw exception;
        }
    }

    @Override
    public List<IotDeviceMessageDO> getDeviceMessageListByRequestIdsAndReply(Long deviceId, List<String> requestIds, Boolean reply) {
        if (CollUtil.isEmpty(requestIds)) {
            return ListUtil.of();
        }
        return deviceMessageMapper.selectListByRequestIdsAndReply(deviceId, requestIds, reply);
    }

    @Override
    public Long getDeviceMessageCount(LocalDateTime createTime) {
        return deviceMessageMapper.selectCountByCreateTime(
                createTime != null ? LocalDateTimeUtil.toEpochMilli(createTime) : null);
    }

    @Override
    public List<IotStatisticsDeviceMessageSummaryByDateRespVO> getDeviceMessageSummaryByDate(
            IotStatisticsDeviceMessageReqVO reqVO) {
        // 1. 按小时统计，获取分项统计数据
        List<Map<String, Object>> countList = deviceMessageMapper.selectDeviceMessageCountGroupByDate(
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
