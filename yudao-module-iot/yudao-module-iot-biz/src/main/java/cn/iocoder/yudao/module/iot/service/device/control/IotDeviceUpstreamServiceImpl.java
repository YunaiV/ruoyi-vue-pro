package cn.iocoder.yudao.module.iot.service.device.control;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.*;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.control.IotDeviceUpstreamReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceMessageTypeEnum;
import cn.iocoder.yudao.module.iot.enums.product.IotProductDeviceTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.data.IotDevicePropertyService;
import cn.iocoder.yudao.module.iot.util.MqttSignUtils;
import cn.iocoder.yudao.module.iot.util.MqttSignUtils.MqttSignResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * IoT 设备上行 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotDeviceUpstreamServiceImpl implements IotDeviceUpstreamService {

    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotDevicePropertyService devicePropertyService;

    // TODO @芋艿：需要重新实现下；
    @Override
    @SuppressWarnings("unchecked")
    public void upstreamDevice(IotDeviceUpstreamReqVO simulatorReqVO) {
        // 1. 校验存在
        IotDeviceDO device = deviceService.validateDeviceExists(simulatorReqVO.getId());

        // 2.1 情况一：属性上报
        String requestId = IdUtil.fastSimpleUUID();
        if (Objects.equals(simulatorReqVO.getType(), IotDeviceMessageTypeEnum.PROPERTY.getType())) {
            reportDeviceProperty(((IotDevicePropertyReportReqDTO) new IotDevicePropertyReportReqDTO()
                    .setRequestId(requestId).setReportTime(LocalDateTime.now())
                    .setProductKey(device.getProductKey()).setDeviceName(device.getDeviceName()))
                    .setProperties((Map<String, Object>) simulatorReqVO.getData()));
            return;
        }
        // 2.2 情况二：事件上报
        if (Objects.equals(simulatorReqVO.getType(), IotDeviceMessageTypeEnum.EVENT.getType())) {
            reportDeviceEvent(((IotDeviceEventReportReqDTO) new IotDeviceEventReportReqDTO().setRequestId(requestId)
                    .setReportTime(LocalDateTime.now())
                    .setProductKey(device.getProductKey()).setDeviceName(device.getDeviceName()))
                    .setIdentifier(simulatorReqVO.getIdentifier())
                    .setParams((Map<String, Object>) simulatorReqVO.getData()));
            return;
        }
        // 2.3 情况三：状态变更
        if (Objects.equals(simulatorReqVO.getType(), IotDeviceMessageTypeEnum.STATE.getType())) {
            // TODO @芋艿：这里未搞完
            return;
        }
        throw new IllegalArgumentException("未知的类型：" + simulatorReqVO.getType());
    }

//    @Override TODO 芋艿：待重新实现
    public void reportDeviceProperty(IotDevicePropertyReportReqDTO reportReqDTO) {
        // 1.1 获得设备
        log.info("[reportDeviceProperty][上报设备属性: {}]", reportReqDTO);
        IotDeviceDO device = deviceService.getDeviceByProductKeyAndDeviceNameFromCache(
                reportReqDTO.getProductKey(), reportReqDTO.getDeviceName());
        if (device == null) {
            log.error("[reportDeviceProperty][设备({}/{})不存在]",
                    reportReqDTO.getProductKey(), reportReqDTO.getDeviceName());
            return;
        }

        // 2. 发送设备消息
//        IotDeviceMessage message = BeanUtils.toBean(reportReqDTO, IotDeviceMessage.class)
//                .setType(IotDeviceMessageTypeEnum.PROPERTY.getType())
//                .setIdentifier(IotDeviceMessageIdentifierEnum.PROPERTY_REPORT.getIdentifier())
//                .setData(reportReqDTO.getProperties());
//        sendDeviceMessage(message, device);
    }

    //    @Override TODO 芋艿：待重新实现
    public void reportDeviceEvent(IotDeviceEventReportReqDTO reportReqDTO) {
        // 1.1 获得设备
        log.info("[reportDeviceEvent][上报设备事件: {}]", reportReqDTO);
        IotDeviceDO device = deviceService.getDeviceByProductKeyAndDeviceNameFromCache(
                reportReqDTO.getProductKey(), reportReqDTO.getDeviceName());
        if (device == null) {
            log.error("[reportDeviceEvent][设备({}/{})不存在]",
                    reportReqDTO.getProductKey(), reportReqDTO.getDeviceName());
            return;
        }

        // 2. 发送设备消息
//        IotDeviceMessage message = BeanUtils.toBean(reportReqDTO, IotDeviceMessage.class)
//                .setType(IotDeviceMessageTypeEnum.EVENT.getType())
//                .setIdentifier(reportReqDTO.getIdentifier())
//                .setData(reportReqDTO.getParams());
//        sendDeviceMessage(message, device);
    }

    //    @Override TODO 芋艿：待重新实现
    public void registerDevice(IotDeviceRegisterReqDTO registerReqDTO) {
        log.info("[registerDevice][注册设备: {}]", registerReqDTO);
        registerDevice0(registerReqDTO.getProductKey(), registerReqDTO.getDeviceName(), null, registerReqDTO);
    }

    private void registerDevice0(String productKey, String deviceName, Long gatewayId,
            IotDeviceUpstreamAbstractReqDTO registerReqDTO) {
        // 1.1 注册设备
        IotDeviceDO device = deviceService.getDeviceByProductKeyAndDeviceNameFromCache(productKey, deviceName);
        boolean registerNew = device == null;
        if (device == null) {
            device = deviceService.createDevice(productKey, deviceName, gatewayId);
            log.info("[registerDevice0][消息({}) 设备({}/{}) 成功注册]", registerReqDTO, productKey, device);
        } else if (gatewayId != null && ObjUtil.notEqual(device.getGatewayId(), gatewayId)) {
            Long deviceId = device.getId();
            TenantUtils.execute(device.getTenantId(),
                    () -> deviceService.updateDeviceGateway(deviceId, gatewayId));
            log.info("[registerDevice0][消息({}) 设备({}/{}) 更新网关设备编号({})]",
                    registerReqDTO, productKey, device, gatewayId);
        }
        // 1.2 记录设备的最后时间
//        updateDeviceLastTime(device, registerReqDTO);

        // 2. 发送设备消息
        if (registerNew) {
//            IotDeviceMessage message = BeanUtils.toBean(registerReqDTO, IotDeviceMessage.class)
//                    .setType(IotDeviceMessageTypeEnum.REGISTER.getType())
//                    .setIdentifier(IotDeviceMessageIdentifierEnum.REGISTER_REGISTER.getIdentifier());
//            sendDeviceMessage(message, device);
        }
    }

    //    @Override TODO 芋艿：待重新实现
    public void registerSubDevice(IotDeviceRegisterSubReqDTO registerReqDTO) {
        // 1.1 注册子设备
        log.info("[registerSubDevice][注册子设备: {}]", registerReqDTO);
        IotDeviceDO device = deviceService.getDeviceByProductKeyAndDeviceNameFromCache(
                registerReqDTO.getProductKey(), registerReqDTO.getDeviceName());
        if (device == null) {
            log.error("[registerSubDevice][设备({}/{}) 不存在]",
                    registerReqDTO.getProductKey(), registerReqDTO.getDeviceName());
            return;
        }
        if (!IotProductDeviceTypeEnum.isGateway(device.getDeviceType())) {
            log.error("[registerSubDevice][设备({}/{}) 不是网关设备({})，无法进行注册]",
                    registerReqDTO.getProductKey(), registerReqDTO.getDeviceName(), device);
            return;
        }
        // 1.2 记录设备的最后时间
//        updateDeviceLastTime(device, registerReqDTO);

        // 2. 处理子设备
        if (CollUtil.isNotEmpty(registerReqDTO.getParams())) {
            registerReqDTO.getParams().forEach(subDevice -> registerDevice0(
                    subDevice.getProductKey(), subDevice.getDeviceName(), device.getId(), registerReqDTO));
            // TODO @芋艿：后续要处理，每个设备是否成功
        }

        // 3. 发送设备消息
//        IotDeviceMessage message = BeanUtils.toBean(registerReqDTO, IotDeviceMessage.class)
//                .setType(IotDeviceMessageTypeEnum.REGISTER.getType())
//                .setIdentifier(IotDeviceMessageIdentifierEnum.REGISTER_REGISTER_SUB.getIdentifier())
//                .setData(registerReqDTO.getParams());
//        sendDeviceMessage(message, device);
    }

    //    @Override TODO 芋艿：待重新实现
    public void addDeviceTopology(IotDeviceTopologyAddReqDTO addReqDTO) {
        // 1.1 获得设备
        log.info("[addDeviceTopology][添加设备拓扑: {}]", addReqDTO);
        IotDeviceDO device = deviceService.getDeviceByProductKeyAndDeviceNameFromCache(
                addReqDTO.getProductKey(), addReqDTO.getDeviceName());
        if (device == null) {
            log.error("[addDeviceTopology][设备({}/{}) 不存在]",
                    addReqDTO.getProductKey(), addReqDTO.getDeviceName());
            return;
        }
        if (!IotProductDeviceTypeEnum.isGateway(device.getDeviceType())) {
            log.error("[addDeviceTopology][设备({}/{}) 不是网关设备({})，无法进行拓扑添加]",
                    addReqDTO.getProductKey(), addReqDTO.getDeviceName(), device);
            return;
        }

        // 2. 处理拓扑
        if (CollUtil.isNotEmpty(addReqDTO.getParams())) {
            TenantUtils.execute(device.getTenantId(), () -> {
                addReqDTO.getParams().forEach(subDevice -> {
                    IotDeviceDO subDeviceDO = deviceService.getDeviceByProductKeyAndDeviceNameFromCache(
                            subDevice.getProductKey(), subDevice.getDeviceName());
                    // TODO @芋艿：后续要处理，每个设备是否成功
                    if (subDeviceDO == null) {
                        log.error("[addDeviceTopology][子设备({}/{}) 不存在]",
                                subDevice.getProductKey(), subDevice.getDeviceName());
                        return;
                    }
                    deviceService.updateDeviceGateway(subDeviceDO.getId(), device.getId());
                    log.info("[addDeviceTopology][子设备({}/{}) 添加到网关设备({}) 成功]",
                            subDevice.getProductKey(), subDevice.getDeviceName(), device);
                });
            });
        }

        // 3. 发送设备消息
//        IotDeviceMessage message = BeanUtils.toBean(addReqDTO, IotDeviceMessage.class)
//                .setType(IotDeviceMessageTypeEnum.TOPOLOGY.getType())
//                .setIdentifier(IotDeviceMessageIdentifierEnum.TOPOLOGY_ADD.getIdentifier())
//                .setData(addReqDTO.getParams());
//        sendDeviceMessage(message, device);
    }

    // TODO @芋艿：后续需要考虑，http 的认证
    @Override
    public boolean authenticateEmqxConnection(IotDeviceEmqxAuthReqDTO authReqDTO) {
        log.info("[authenticateEmqxConnection][认证 Emqx 连接: {}]", authReqDTO);
        // 1.1 校验设备是否存在。username 格式：${DeviceName}&${ProductKey}
        String[] usernameParts = authReqDTO.getUsername().split("&");
        if (usernameParts.length != 2) {
            log.error("[authenticateEmqxConnection][认证失败，username 格式不正确]");
            return false;
        }
        String deviceName = usernameParts[0];
        String productKey = usernameParts[1];
        // 1.2 获得设备
        IotDeviceDO device = deviceService.getDeviceByProductKeyAndDeviceNameFromCache(productKey, deviceName);
        if (device == null) {
            log.error("[authenticateEmqxConnection][设备({}/{}) 不存在]", productKey, deviceName);
            return false;
        }
        // TODO @haohao：需要记录，记录设备的最后时间

        // 2. 校验密码
        String deviceSecret = device.getDeviceSecret();
        String clientId = authReqDTO.getClientId();
        MqttSignResult sign = MqttSignUtils.calculate(productKey, deviceName, deviceSecret, clientId);
        // TODO 建议，先失败，return false；
        if (StrUtil.equals(sign.getPassword(), authReqDTO.getPassword())) {
            log.info("[authenticateEmqxConnection][认证成功]");
            return true;
        }
        log.error("[authenticateEmqxConnection][认证失败，密码不正确]");
        return false;
    }

}
