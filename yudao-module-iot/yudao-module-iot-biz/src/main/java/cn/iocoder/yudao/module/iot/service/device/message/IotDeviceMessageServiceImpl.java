package cn.iocoder.yudao.module.iot.service.device.message;

import cn.hutool.core.date.LocalDateTimeUtil;
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
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceMessageDO;
import cn.iocoder.yudao.module.iot.dal.tdengine.IotDeviceMessageMapper;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.Objects;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

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
                .setReply(IotDeviceMessageUtils.isReplyMessage(message));
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

    // TODO @芋艿：针对连接网关的设备，是不是 productKey、deviceName 需要调整下；
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

        // 3. 回复消息。前提：非 _reply 消息，并且非禁用回复的消息
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
    @SuppressWarnings("SameReturnValue")
    private Object handleUpstreamDeviceMessage0(IotDeviceMessage message, IotDeviceDO device) {
        // 设备上线
        if (Objects.equal(message.getMethod(), IotDeviceMessageMethodEnum.STATE_ONLINE.getMethod())) {
            deviceService.updateDeviceState(device, IotDeviceStateEnum.ONLINE.getState());
            // TODO 芋艿：子设备的关联
            return null;
        }
        // 设备下线
        if (Objects.equal(message.getMethod(), IotDeviceMessageMethodEnum.STATE_OFFLINE.getMethod())) {
            deviceService.updateDeviceState(device, IotDeviceStateEnum.OFFLINE.getState());
            // TODO 芋艿：子设备的关联
            return null;
        }

        // 属性上报
        if (Objects.equal(message.getMethod(), IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod())) {
            devicePropertyService.saveDeviceProperty(device, message);
            return null;
        }

        // TODO @芋艿：这里可以按需，添加别的逻辑；
        return null;
    }

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
    public Long getDeviceMessageCount(LocalDateTime createTime) {
        return deviceMessageMapper.selectCountByCreateTime(createTime != null ? LocalDateTimeUtil.toEpochMilli(createTime) : null);
    }

    @Override
    public List<IotStatisticsDeviceMessageSummaryByDateRespVO> getDeviceMessageSummaryByDate(
            IotStatisticsDeviceMessageReqVO reqVO) {
        // 1. 按小时统计，获取分项统计数据
        List<Map<String, Object>> countList = deviceMessageMapper.selectDeviceMessageCountGroupByDate(
                LocalDateTimeUtil.toEpochMilli(reqVO.getTimes()[0]), LocalDateTimeUtil.toEpochMilli(reqVO.getTimes()[1]));

        // 2. 按照日期间隔，合并数据
        List<LocalDateTime[]> timeRanges = LocalDateTimeUtils.getDateRangeList(reqVO.getTimes()[0], reqVO.getTimes()[1], reqVO.getInterval());
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
