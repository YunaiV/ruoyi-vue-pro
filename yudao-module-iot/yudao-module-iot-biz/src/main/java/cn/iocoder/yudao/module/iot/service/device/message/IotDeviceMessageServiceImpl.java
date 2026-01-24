package cn.iocoder.yudao.module.iot.service.device.message;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.IotDeviceSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.message.IotDeviceMessagePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.statistics.vo.IotStatisticsDeviceMessageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.statistics.vo.IotStatisticsDeviceMessageSummaryByDateRespVO;
import cn.iocoder.yudao.module.iot.core.biz.dto.*;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertyPackPostReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotSubDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotSubDeviceRegisterRespDTO;
import cn.iocoder.yudao.module.iot.core.topic.topo.IotDeviceTopoAddReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.topo.IotDeviceTopoDeleteReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.topo.IotDeviceTopoRespDTO;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceMessageDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.tdengine.IotDeviceMessageMapper;
import cn.iocoder.yudao.module.iot.enums.product.IotProductDeviceTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import cn.iocoder.yudao.module.iot.service.ota.IotOtaTaskRecordService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.Objects;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

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
    private IotProductService productService;

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
            // 特殊：网关设备下线时，网关子设备联动下线
            if (Objects.equal(state, IotDeviceStateEnum.OFFLINE.getState())
                    && IotProductDeviceTypeEnum.isGateway(device.getDeviceType())) {
                handleGatewayOffline(device, message.getServerId());
            }
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
            return handleTopoAdd(message, device);
        }
        // 删除拓扑关系
        if (Objects.equal(message.getMethod(), IotDeviceMessageMethodEnum.TOPO_DELETE.getMethod())) {
            return handleTopoDelete(message, device);
        }
        // 获取拓扑关系
        if (Objects.equal(message.getMethod(), IotDeviceMessageMethodEnum.TOPO_GET.getMethod())) {
            return handleTopoGet(device);
        }

        // 子设备动态注册
        if (Objects.equal(message.getMethod(), IotDeviceMessageMethodEnum.SUB_DEVICE_REGISTER.getMethod())) {
            return handleSubDeviceRegister(message, device);
        }

        return null;
    }

    // ========== 拓扑管理处理方法 ==========

    // TODO @AI：是不是更适合在 deviceService 里面处理？
    /**
     * 处理添加拓扑关系请求
     *
     * @param message       消息
     * @param gatewayDevice 网关设备
     * @return 响应数据
     */
    private Object handleTopoAdd(IotDeviceMessage message, IotDeviceDO gatewayDevice) {
        // 1.1 校验网关设备类型
        if (!IotProductDeviceTypeEnum.isGateway(gatewayDevice.getDeviceType())) {
            throw exception(DEVICE_NOT_GATEWAY);
        }
        // 1.2 解析参数
        // TODO @AI：是不是 parseObject 增加一个方法，允许传入 object 类型，避免先转 jsonString 再 parseObject ；
        IotDeviceTopoAddReqDTO params = JsonUtils.parseObject(JsonUtils.toJsonString(message.getParams()),
                IotDeviceTopoAddReqDTO.class);
        if (params == null || CollUtil.isEmpty(params.getSubDevices())) {
            throw exception(DEVICE_TOPO_PARAMS_INVALID);
        }

        // 2. 遍历处理每个子设备
        // TODO @AI：processTopoAddSubDevice 不要抽成小方法；
        List<IotDeviceDO> addedSubDevices = new ArrayList<>(params.getSubDevices().size());
        for (IotDeviceAuthReqDTO subDeviceAuth : params.getSubDevices()) {
            try {
                IotDeviceDO subDevice = processTopoAddSubDevice(subDeviceAuth, gatewayDevice);
                addedSubDevices.add(subDevice);
            } catch (Exception ex) {
                log.warn("[handleTopoAdd][网关({}/{}) 添加子设备失败，username={}]",
                        gatewayDevice.getProductKey(), gatewayDevice.getDeviceName(),
                        subDeviceAuth.getUsername(), ex);
            }
        }
        // TODO @AI：http://help.aliyun.com/zh/marketplace/add-topological-relationship 要回复的！

        // 3. 发送拓扑变更通知
        // TODO @AI：这里不应该发，它更多发生在，管理后台改动后，主动下发；http://help.aliyun.com/zh/marketplace/notify-gateway-topology-changes
        for (IotDeviceDO subDevice : addedSubDevices) {
            sendTopoChangeNotify(gatewayDevice, "add", subDevice);
        }
        return null;
    }

    /**
     * 处理单个子设备的拓扑添加
     *
     * @param subDeviceAuth 子设备认证信息
     * @param gatewayDevice 网关设备
     * @return 添加成功的子设备，失败返回 null
     */
    private IotDeviceDO processTopoAddSubDevice(IotDeviceAuthReqDTO subDeviceAuth, IotDeviceDO gatewayDevice) {
        // 1.1 解析子设备信息
        IotDeviceAuthUtils.DeviceInfo subDeviceInfo = IotDeviceAuthUtils.parseUsername(subDeviceAuth.getUsername());
        if (subDeviceInfo == null) {
            throw exception(DEVICE_TOPO_SUB_DEVICE_USERNAME_INVALID);
        }
        // 1.2 校验子设备认证信息
        if (!deviceService.authDevice(subDeviceAuth)) {
            throw exception(DEVICE_TOPO_SUB_DEVICE_AUTH_FAILED);
        }

        // 1.3 获取子设备
        IotDeviceDO subDevice = deviceService.getDeviceFromCache(subDeviceInfo.getProductKey(), subDeviceInfo.getDeviceName());
        if (subDevice == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        // 1.4 校验子设备类型
        if (!IotProductDeviceTypeEnum.isGatewaySub(subDevice.getDeviceType())) {
            throw exception(DEVICE_NOT_GATEWAY_SUB, subDevice.getProductKey(), subDevice.getDeviceName());
        }
        // 1.5 校验子设备是否已绑定到其他网关
        if (subDevice.getGatewayId() != null
                && ObjectUtil.notEqual(subDevice.getGatewayId(), gatewayDevice.getId())) {
            throw exception(DEVICE_GATEWAY_BINDTO_EXISTS, subDevice.getProductKey(), subDevice.getDeviceName());
        }

        // 2. 绑定拓扑关系
        // TODO @AI：上面的校验，貌似和 bindDeviceGateway 里的，有点重复；
        deviceService.bindDeviceGateway(Collections.singletonList(subDevice.getId()), gatewayDevice.getId());
        log.info("[processTopoAddSubDevice][网关({}/{}) 绑定子设备({}/{})]",
                gatewayDevice.getProductKey(), gatewayDevice.getDeviceName(),
                subDevice.getProductKey(), subDevice.getDeviceName());
        return subDevice;
    }

    // TODO @AI：是不是更适合在 deviceService 里面处理？

    /**
     * 处理删除拓扑关系请求
     *
     * @param message       消息
     * @param gatewayDevice 网关设备
     * @return 响应数据
     */
    private Object handleTopoDelete(IotDeviceMessage message, IotDeviceDO gatewayDevice) {
        // 1. 校验网关设备类型
        if (!IotProductDeviceTypeEnum.isGateway(gatewayDevice.getDeviceType())) {
            throw exception(DEVICE_NOT_GATEWAY);
        }

        // 2. 解析参数（数组格式）
        IotDeviceTopoDeleteReqDTO params = JsonUtils.parseObject(JsonUtils.toJsonString(message.getParams()),
                IotDeviceTopoDeleteReqDTO.class);
        if (params == null || params.getSubDevices() == null || params.getSubDevices().isEmpty()) {
            throw exception(DEVICE_TOPO_PARAMS_INVALID);
        }

        // 3. 遍历处理每个子设备
        List<IotDeviceDO> deletedSubDevices = new ArrayList<>();
        for (IotDeviceTopoDeleteReqDTO.SubDevice subDeviceInfo : params.getSubDevices()) {
            try {
                IotDeviceDO subDevice = processTopoDeleteSubDevice(subDeviceInfo, gatewayDevice);
                deletedSubDevices.add(subDevice);
            } catch (Exception ex) {
                log.warn("[handleTopoDelete][网关({}/{}) 删除子设备失败，productKey={}, deviceName={}]",
                        gatewayDevice.getProductKey(), gatewayDevice.getDeviceName(),
                        subDeviceInfo.getProductKey(), subDeviceInfo.getDeviceName(), ex);
            }
        }

        // 4. 发送拓扑变更通知
        for (IotDeviceDO subDevice : deletedSubDevices) {
            sendTopoChangeNotify(gatewayDevice, "delete", subDevice);
        }
        return null;
    }

    // TODO @AI：是不是更适合在 deviceService 里面处理？

    /**
     * 处理单个子设备的拓扑删除
     *
     * @param subDeviceInfo 子设备标识
     * @param gatewayDevice 网关设备
     * @return 删除成功的子设备，失败返回 null
     */
    private IotDeviceDO processTopoDeleteSubDevice(IotDeviceTopoDeleteReqDTO.SubDevice subDeviceInfo,
                                                    IotDeviceDO gatewayDevice) {
        // 1. 获取子设备
        IotDeviceDO subDevice = deviceService.getDeviceFromCache(subDeviceInfo.getProductKey(), subDeviceInfo.getDeviceName());
        if (subDevice == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }

        // 2. 校验子设备是否绑定到该网关
        if (!Objects.equal(subDevice.getGatewayId(), gatewayDevice.getId())) {
            throw exception(DEVICE_TOPO_SUB_NOT_BINDTO_GATEWAY, subDeviceInfo.getProductKey(), subDeviceInfo.getDeviceName());
        }

        // 3. 解绑拓扑关系
        deviceService.unbindDeviceGateway(Collections.singletonList(subDevice.getId()));
        log.info("[processTopoDeleteSubDevice][网关({}/{}) 解绑子设备({}/{})]",
                gatewayDevice.getProductKey(), gatewayDevice.getDeviceName(),
                subDevice.getProductKey(), subDevice.getDeviceName());

        // 4. 子设备下线
        if (Objects.equal(subDevice.getState(), IotDeviceStateEnum.ONLINE.getState())) {
            deviceService.updateDeviceState(subDevice, IotDeviceStateEnum.OFFLINE.getState());
        }

        return subDevice;
    }

    // TODO @AI：是不是更适合在 deviceService 里面处理？
    /**
     * 处理获取拓扑关系请求
     *
     * @param gatewayDevice 网关设备
     * @return 子设备列表
     */
    private Object handleTopoGet(IotDeviceDO gatewayDevice) {
        // 1. 校验网关设备类型
        if (!IotProductDeviceTypeEnum.isGateway(gatewayDevice.getDeviceType())) {
            throw exception(DEVICE_NOT_GATEWAY);
        }

        // 2. 获取子设备列表
        List<IotDeviceDO> subDevices = deviceService.getDeviceListByGatewayId(gatewayDevice.getId());

        // 3. 转换为响应格式
        return convertList(subDevices, subDevice -> new IotDeviceTopoRespDTO()
                .setProductKey(subDevice.getProductKey())
                .setDeviceName(subDevice.getDeviceName()));
    }

    // TODO @AI：是不是更适合在 deviceService 里面处理？
    /**
     * 发送拓扑变更通知
     *
     * @param gatewayDevice 网关设备
     * @param changeType    变更类型：add/delete
     * @param subDevice     子设备
     */
    private void sendTopoChangeNotify(IotDeviceDO gatewayDevice, String changeType, IotDeviceDO subDevice) {
        try {
            String serverId = devicePropertyService.getDeviceServerId(gatewayDevice.getId());
            if (StrUtil.isEmpty(serverId)) {
                log.warn("[sendTopoChangeNotify][网关({}/{}) serverId 为空，无法发送拓扑变更通知]",
                        gatewayDevice.getProductKey(), gatewayDevice.getDeviceName());
                return;
            }

            Map<String, Object> params = MapUtil.builder(new HashMap<String, Object>())
                    .put("changeType", changeType)
                    .put("subDevice", MapUtil.builder(new HashMap<String, Object>())
                            .put("productKey", subDevice.getProductKey())
                            .put("deviceName", subDevice.getDeviceName())
                            .build())
                    .build();

            IotDeviceMessage notifyMessage = IotDeviceMessage.requestOf(
                    IotDeviceMessageMethodEnum.TOPO_CHANGE.getMethod(), params);
            sendDeviceMessage(notifyMessage, gatewayDevice, serverId);
        } catch (Exception ex) {
            log.error("[sendTopoChangeNotify][发送拓扑变更通知失败，网关({}/{}), 子设备({}/{})]",
                    gatewayDevice.getProductKey(), gatewayDevice.getDeviceName(),
                    subDevice.getProductKey(), subDevice.getDeviceName(), ex);
        }
    }

    // ========== 子设备注册处理方法 ==========

    // TODO @AI：是不是更适合在 deviceService 里面处理？
    /**
     * 处理子设备动态注册请求
     *
     * @param message       消息
     * @param gatewayDevice 网关设备
     * @return 注册结果列表
     */
    private Object handleSubDeviceRegister(IotDeviceMessage message, IotDeviceDO gatewayDevice) {
        // 1. 校验网关设备类型
        if (!IotProductDeviceTypeEnum.isGateway(gatewayDevice.getDeviceType())) {
            throw exception(DEVICE_NOT_GATEWAY);
        }

        // 2. 解析参数（数组）
        List<IotSubDeviceRegisterReqDTO> paramsList;
        if (message.getParams() instanceof List) {
            paramsList = JsonUtils.parseArray(JsonUtils.toJsonString(message.getParams()),
                    IotSubDeviceRegisterReqDTO.class);
        } else {
            throw exception(DEVICE_SUB_REGISTER_PARAMS_INVALID);
        }

        if (paramsList == null || paramsList.isEmpty()) {
            throw exception(DEVICE_SUB_REGISTER_PARAMS_INVALID);
        }

        // 3. 遍历注册每个子设备
        List<IotSubDeviceRegisterRespDTO> results = new ArrayList<>();
        for (IotSubDeviceRegisterReqDTO params : paramsList) {
            try {
                IotSubDeviceRegisterRespDTO result = registerSubDevice(params, gatewayDevice);
                results.add(result);
            } catch (Exception ex) {
                log.error("[handleSubDeviceRegister][子设备({}/{}) 注册失败]",
                        params.getProductKey(), params.getDeviceName(), ex);
                // 继续处理其他子设备，不影响整体流程
            }
        }

        return results;
    }

    // TODO @AI：是不是更适合在 deviceService 里面处理？
    /**
     * 注册单个子设备
     *
     * @param params        注册参数
     * @param gatewayDevice 网关设备
     * @return 注册结果
     */
    private IotSubDeviceRegisterRespDTO registerSubDevice(IotSubDeviceRegisterReqDTO params,
                                                          IotDeviceDO gatewayDevice) {
        // 1. 查找产品
        IotProductDO product = productService.getProductByProductKey(params.getProductKey());
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }

        // 2. 校验产品是否为网关子设备类型
        if (!IotProductDeviceTypeEnum.isGatewaySub(product.getDeviceType())) {
            throw exception(DEVICE_SUB_REGISTER_PRODUCT_NOT_GATEWAY_SUB, params.getProductKey());
        }

        // 3. 查找设备是否已存在
        IotDeviceDO existDevice = deviceService.getDeviceFromCache(params.getProductKey(), params.getDeviceName());
        if (existDevice != null) {
            // 已存在则返回设备信息
            return new IotSubDeviceRegisterRespDTO()
                    .setProductKey(existDevice.getProductKey())
                    .setDeviceName(existDevice.getDeviceName())
                    .setDeviceSecret(existDevice.getDeviceSecret());
        }

        // 4. 创建新设备
        IotDeviceSaveReqVO createReqVO = new IotDeviceSaveReqVO()
                .setDeviceName(params.getDeviceName())
                .setProductId(product.getId())
                .setGatewayId(gatewayDevice.getId());
        Long deviceId = deviceService.createDevice(createReqVO);

        // 5. 获取新创建的设备信息
        IotDeviceDO newDevice = deviceService.getDevice(deviceId);
        log.info("[registerSubDevice][网关({}/{}) 注册子设备({}/{})]",
                gatewayDevice.getProductKey(), gatewayDevice.getDeviceName(),
                newDevice.getProductKey(), newDevice.getDeviceName());

        return new IotSubDeviceRegisterRespDTO()
                .setProductKey(newDevice.getProductKey())
                .setDeviceName(newDevice.getDeviceName())
                .setDeviceSecret(newDevice.getDeviceSecret());
    }

    // ========== 批量上报处理方法 ==========

    /**
     * 处理批量上报消息
     * <p>
     * 将 pack 消息拆分成多条标准消息，递归处理
     *
     * @param packMessage   批量消息
     * @param gatewayDevice 网关设备
     */
    private void handlePackMessage(IotDeviceMessage packMessage, IotDeviceDO gatewayDevice) {
        // 1. 解析参数
        IotDevicePropertyPackPostReqDTO params = JsonUtils.parseObject(
                JsonUtils.toJsonString(packMessage.getParams()),
                IotDevicePropertyPackPostReqDTO.class);
        if (params == null) {
            log.warn("[handlePackMessage][消息({}) 参数解析失败]", packMessage.getId());
            return;
        }

        // 2. 处理网关自身属性
        // TODO @AI：是不是经过总线会更好：
        // TODO @AI：是不是少处理了 event 事件？
        if (params.getProperties() != null && !params.getProperties().isEmpty()) {
            Map<String, Object> gatewayProperties = convertPackProperties(params.getProperties());
            IotDeviceMessage gatewayMsg = IotDeviceMessage.builder()
                    .id(IotDeviceMessageUtils.generateMessageId())
                    .deviceId(gatewayDevice.getId())
                    .tenantId(gatewayDevice.getTenantId())
                    .serverId(packMessage.getServerId())
                    .method(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod())
                    .params(gatewayProperties)
                    .reportTime(LocalDateTime.now())
                    .build();
            // 直接调用处理，不通过消息总线
            try {
                devicePropertyService.saveDeviceProperty(gatewayDevice, gatewayMsg);
                getSelf().createDeviceLogAsync(gatewayMsg);
            } catch (Exception ex) {
                log.error("[handlePackMessage][网关({}) 属性处理失败]", gatewayDevice.getId(), ex);
            }
        }

        // 3. 处理子设备数据
        if (params.getSubDevices() != null) {
            for (IotDevicePropertyPackPostReqDTO.SubDeviceData subDeviceData : params.getSubDevices()) {
                try {
                    handleSubDevicePackData(packMessage, subDeviceData);
                } catch (Exception ex) {
                    log.error("[handlePackMessage][子设备({}/{}) 数据处理失败]",
                            subDeviceData.getIdentity().getProductKey(),
                            subDeviceData.getIdentity().getDeviceName(), ex);
                }
            }
        }
    }

    /**
     * 处理子设备的 pack 数据
     *
     * @param packMessage   原始 pack 消息
     * @param subDeviceData 子设备数据
     */
    private void handleSubDevicePackData(IotDeviceMessage packMessage,
                                         IotDevicePropertyPackPostReqDTO.SubDeviceData subDeviceData) {
        // 1. 获取子设备
        IotDevicePropertyPackPostReqDTO.DeviceIdentity identity = subDeviceData.getIdentity();
        IotDeviceDO subDevice = deviceService.getDeviceFromCache(identity.getProductKey(), identity.getDeviceName());
        if (subDevice == null) {
            log.warn("[handleSubDevicePackData][子设备({}/{}) 不存在]",
                    identity.getProductKey(), identity.getDeviceName());
            return;
        }

        // 2. 处理子设备属性
        if (subDeviceData.getProperties() != null && !subDeviceData.getProperties().isEmpty()) {
            Map<String, Object> properties = convertPackProperties(subDeviceData.getProperties());
            IotDeviceMessage subMsg = IotDeviceMessage.builder()
                    .id(IotDeviceMessageUtils.generateMessageId())
                    .deviceId(subDevice.getId())
                    .tenantId(subDevice.getTenantId())
                    .serverId(packMessage.getServerId())
                    .method(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod())
                    .params(properties)
                    .reportTime(LocalDateTime.now())
                    .build();
            devicePropertyService.saveDeviceProperty(subDevice, subMsg);
            getSelf().createDeviceLogAsync(subMsg);
        }

        // 3. 处理子设备事件
        // TODO @AI：事件处理可以后续扩展
    }

    /**
     * 转换 pack 属性格式为标准属性格式
     * <p>
     * pack 格式：{"temperature": {"value": 25.5, "time": 1524448722000}}
     * 标准格式：{"temperature": 25.5}
     *
     * @param packProperties pack 属性
     * @return 标准属性
     */
    private Map<String, Object> convertPackProperties(Map<String, IotDevicePropertyPackPostReqDTO.PropertyValue> packProperties) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, IotDevicePropertyPackPostReqDTO.PropertyValue> entry : packProperties.entrySet()) {
            if (entry.getValue() != null) {
                result.put(entry.getKey(), entry.getValue().getValue());
            }
        }
        return result;
    }

    // ========== 网关下线联动处理 ==========

    // TODO 芋艿：是不是写到 deviceService 里更合适？更解耦。
    /**
     * 处理网关下线，联动所有子设备下线
     *
     * @param gatewayDevice 网关设备
     * @param serverId      服务标识
     */
    private void handleGatewayOffline(IotDeviceDO gatewayDevice, String serverId) {
        // 1. 获取网关下所有子设备
        List<IotDeviceDO> subDevices = deviceService.getDeviceListByGatewayId(gatewayDevice.getId());
        if (subDevices == null || subDevices.isEmpty()) {
            return;
        }

        // 2. 将在线的子设备设置为下线
        for (IotDeviceDO subDevice : subDevices) {
            if (Objects.equal(subDevice.getState(), IotDeviceStateEnum.ONLINE.getState())) {
                try {
                    deviceService.updateDeviceState(subDevice, IotDeviceStateEnum.OFFLINE.getState());
                    log.info("[handleGatewayOffline][网关({}/{}) 下线，子设备({}/{}) 联动下线]",
                            gatewayDevice.getProductKey(), gatewayDevice.getDeviceName(),
                            subDevice.getProductKey(), subDevice.getDeviceName());
                } catch (Exception ex) {
                    log.error("[handleGatewayOffline][子设备({}/{}) 下线失败]",
                            subDevice.getProductKey(), subDevice.getDeviceName(), ex);
                }
            }
        }
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
    public List<IotDeviceMessageDO> getDeviceMessageListByRequestIdsAndReply(Long deviceId,
                                                                             List<String> requestIds,
                                                                             Boolean reply) {
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
