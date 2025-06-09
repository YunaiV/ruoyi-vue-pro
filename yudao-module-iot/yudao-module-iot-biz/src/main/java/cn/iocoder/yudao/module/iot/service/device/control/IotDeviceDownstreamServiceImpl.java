package cn.iocoder.yudao.module.iot.service.device.control;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.control.IotDeviceDownstreamReqVO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceMessageIdentifierEnum;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceMessageTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.data.IotDevicePropertyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.DEVICE_DOWNSTREAM_FAILED_SERVER_ID_NULL;

/**
 * IoT 设备下行 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotDeviceDownstreamServiceImpl implements IotDeviceDownstreamService {

    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotDevicePropertyService devicePropertyService;

    @Resource
    private IotDeviceMessageProducer deviceMessageProducer;

    @Override
    public IotDeviceMessage downstreamDevice(IotDeviceDownstreamReqVO downstreamReqVO) {
        // 1. 校验设备是否存在
        IotDeviceDO device = deviceService.validateDeviceExists(downstreamReqVO.getId());
        // TODO 芋艿：父设备的处理
        IotDeviceDO parentDevice = null;

        // 2. 构建消息
        IotDeviceMessage message = buildDownstreamDeviceMessage(downstreamReqVO, device, parentDevice);

        // 3.1 发送给网关
        String serverId = devicePropertyService.getDeviceServerId(message.getProductKey(), message.getDeviceName());
        if (StrUtil.isEmpty(serverId)) {
            throw exception(DEVICE_DOWNSTREAM_FAILED_SERVER_ID_NULL);
        }
        deviceMessageProducer.sendDeviceMessageToGateway(serverId, message);

        // 3.2 发送给服务器（用于设备日志等的记录）
        deviceMessageProducer.sendDeviceMessage(message);
        return message;
    }

    @SuppressWarnings("unchecked")
    private IotDeviceMessage buildDownstreamDeviceMessage(IotDeviceDownstreamReqVO downstreamReqVO,
                                                          IotDeviceDO device, IotDeviceDO parentDevice) {
        IotDeviceMessage message = IotDeviceMessage.of(getProductKey(device, parentDevice),
                getDeviceName(device, parentDevice));
        // 服务调用
        if (Objects.equals(downstreamReqVO.getType(), IotDeviceMessageTypeEnum.SERVICE.getType())) {
            // TODO @芋艿：待实现
//            return invokeDeviceService(downstreamReqVO, device, parentDevice);
        }
        // 属性相关
        if (Objects.equals(downstreamReqVO.getType(), IotDeviceMessageTypeEnum.PROPERTY.getType())) {
            // 属性设置
            if (Objects.equals(downstreamReqVO.getIdentifier(),
                    IotDeviceMessageIdentifierEnum.PROPERTY_SET.getIdentifier())) {
                if (!(downstreamReqVO.getData() instanceof Map<?, ?>)) {
                    throw new ServiceException(BAD_REQUEST.getCode(), "data 不是 Map 类型");
                }
                return message.ofPropertySet((Map<String, Object>) downstreamReqVO.getData());
            }
            // 属性获取
            if (Objects.equals(downstreamReqVO.getIdentifier(),
                    IotDeviceMessageIdentifierEnum.PROPERTY_GET.getIdentifier())) {
                // TODO @芋艿：待实现
//                return getDeviceProperty(downstreamReqVO, device, parentDevice);
            }
        }
        // 配置下发
        if (Objects.equals(downstreamReqVO.getType(), IotDeviceMessageTypeEnum.CONFIG.getType())
                && Objects.equals(downstreamReqVO.getIdentifier(),
                IotDeviceMessageIdentifierEnum.CONFIG_SET.getIdentifier())) {
            // TODO @芋艿：待实现
//            return setDeviceConfig(downstreamReqVO, device, parentDevice);
        }
        // OTA 升级
        if (Objects.equals(downstreamReqVO.getType(), IotDeviceMessageTypeEnum.OTA.getType())) {
            // TODO @芋艿：待实现
//            return otaUpgrade(downstreamReqVO, device, parentDevice);
        }
        // TODO @芋艿：取消设备的网关的时，要不要下发 REGISTER_UNREGISTER_SUB ？
        throw new IllegalArgumentException("不支持的下行消息类型：" + downstreamReqVO);
    }

//    /**
//     * 调用设备服务
//     *
//     * @param downstreamReqVO 下行请求
//     * @param device          设备
//     * @param parentDevice    父设备
//     * @return 下发消息
//     */
//    @SuppressWarnings("unchecked")
//    private IotDeviceMessage invokeDeviceService(IotDeviceDownstreamReqVO downstreamReqVO,
//                                                 IotDeviceDO device, IotDeviceDO parentDevice) {
//        // 1. 参数校验
//        if (!(downstreamReqVO.getData() instanceof Map<?, ?>)) {
//            throw new ServiceException(BAD_REQUEST.getCode(), "data 不是 Map 类型");
//        }
//        // TODO @super：【可优化】过滤掉不合法的服务
//
//        // 2. 发送请求
//        String url = String.format("sys/%s/%s/thing/service/%s",
//                getProductKey(device, parentDevice), getDeviceName(device, parentDevice),
//                downstreamReqVO.getIdentifier());
//        IotDeviceServiceInvokeReqDTO reqDTO = new IotDeviceServiceInvokeReqDTO()
//                .setParams((Map<String, Object>) downstreamReqVO.getData());
////        CommonResult<Boolean> result = requestPlugin(url, reqDTO, device);
//        CommonResult<Boolean> result = null;
//
//        // 3. 发送设备消息
//        IotDeviceMessage message = new IotDeviceMessage().setRequestId(reqDTO.getRequestId())
//                .setType(IotDeviceMessageTypeEnum.SERVICE.getType()).setIdentifier(reqDTO.getIdentifier())
//                .setData(reqDTO.getParams());
//        sendDeviceMessage(message, device, result.getCode());
//
//        // 4. 如果不成功，抛出异常，提示用户
//        if (result.isError()) {
//            log.error("[invokeDeviceService][设备({})服务调用失败，请求参数：({})，响应结果：({})]",
//                    device.getDeviceKey(), reqDTO, result);
//            throw exception(DEVICE_DOWNSTREAM_FAILED, result.getMsg());
//        }
//        return message;
//    }

//    /**
//     * 获取设备属性
//     *
//     * @param downstreamReqVO 下行请求
//     * @param device          设备
//     * @param parentDevice    父设备
//     * @return 下发消息
//     */
//    @SuppressWarnings("unchecked")
//    private IotDeviceMessage getDeviceProperty(IotDeviceDownstreamReqVO downstreamReqVO,
//                                               IotDeviceDO device, IotDeviceDO parentDevice) {
//        // 1. 参数校验
//        if (!(downstreamReqVO.getData() instanceof List<?>)) {
//            throw new ServiceException(BAD_REQUEST.getCode(), "data 不是 List 类型");
//        }
//        // TODO @super：【可优化】过滤掉不合法的属性
//
//        // 2. 发送请求
//        String url = String.format("sys/%s/%s/thing/service/property/get",
//                getProductKey(device, parentDevice), getDeviceName(device, parentDevice));
//        IotDevicePropertyGetReqDTO reqDTO = new IotDevicePropertyGetReqDTO()
//                .setIdentifiers((List<String>) downstreamReqVO.getData());
////        CommonResult<Boolean> result = requestPlugin(url, reqDTO, device);
//        CommonResult<Boolean> result = null;
//
//        // 3. 发送设备消息
//        IotDeviceMessage message = new IotDeviceMessage().setRequestId(reqDTO.getRequestId())
//                .setType(IotDeviceMessageTypeEnum.PROPERTY.getType())
//                .setIdentifier(IotDeviceMessageIdentifierEnum.PROPERTY_SET.getIdentifier())
//                .setData(reqDTO.getIdentifiers());
//        sendDeviceMessage(message, device, result.getCode());
//
//        // 4. 如果不成功，抛出异常，提示用户
//        if (result.isError()) {
//            log.error("[getDeviceProperty][设备({})属性获取失败，请求参数：({})，响应结果：({})]",
//                    device.getDeviceKey(), reqDTO, result);
//            throw exception(DEVICE_DOWNSTREAM_FAILED, result.getMsg());
//        }
//        return message;
//    }

//    /**
//     * 设置设备配置
//     *
//     * @param downstreamReqVO 下行请求
//     * @param device          设备
//     * @param parentDevice    父设备
//     * @return 下发消息
//     */
//    @SuppressWarnings({ "unchecked", "unused" })
//    private IotDeviceMessage setDeviceConfig(IotDeviceDownstreamReqVO downstreamReqVO,
//                                             IotDeviceDO device, IotDeviceDO parentDevice) {
//        // 1. 参数转换，无需校验
//        Map<String, Object> config = JsonUtils.parseObject(device.getConfig(), Map.class);
//
//        // 2. 发送请求
//        String url = String.format("sys/%s/%s/thing/service/config/set",
//                getProductKey(device, parentDevice), getDeviceName(device, parentDevice));
//        IotDeviceConfigSetReqDTO reqDTO = new IotDeviceConfigSetReqDTO()
//                .setConfig(config);
////        CommonResult<Boolean> result = requestPlugin(url, reqDTO, device);
//        CommonResult<Boolean> result = null;
//
//        // 3. 发送设备消息
//        IotDeviceMessage message = new IotDeviceMessage().setRequestId(reqDTO.getRequestId())
//                .setType(IotDeviceMessageTypeEnum.CONFIG.getType())
//                .setIdentifier(IotDeviceMessageIdentifierEnum.CONFIG_SET.getIdentifier())
//                .setData(reqDTO.getConfig());
//        sendDeviceMessage(message, device, result.getCode());
//
//        // 4. 如果不成功，抛出异常，提示用户
//        if (result.isError()) {
//            log.error("[setDeviceConfig][设备({})配置下发失败，请求参数：({})，响应结果：({})]",
//                    device.getDeviceKey(), reqDTO, result);
//            throw exception(DEVICE_DOWNSTREAM_FAILED, result.getMsg());
//        }
//        return message;
//    }

//    /**
//     * 设备 OTA 升级
//     *
//     * @param downstreamReqVO 下行请求
//     * @param device          设备
//     * @param parentDevice    父设备
//     * @return 下发消息
//     */
//    private IotDeviceMessage otaUpgrade(IotDeviceDownstreamReqVO downstreamReqVO,
//                                        IotDeviceDO device, IotDeviceDO parentDevice) {
//        // 1. 参数校验
//        if (!(downstreamReqVO.getData() instanceof Map<?, ?> data)) {
//            throw new ServiceException(BAD_REQUEST.getCode(), "data 不是 Map 类型");
//        }
//
//        // 2. 发送请求
//        String url = String.format("ota/%s/%s/upgrade",
//                getProductKey(device, parentDevice), getDeviceName(device, parentDevice));
//        IotDeviceOtaUpgradeReqDTO reqDTO = IotDeviceOtaUpgradeReqDTO.build(data);
////        CommonResult<Boolean> result = requestPlugin(url, reqDTO, device);
//        CommonResult<Boolean> result = null;
//
//        // 3. 发送设备消息
//        IotDeviceMessage message = new IotDeviceMessage().setRequestId(reqDTO.getRequestId())
//                .setType(IotDeviceMessageTypeEnum.OTA.getType())
//                .setIdentifier(IotDeviceMessageIdentifierEnum.OTA_UPGRADE.getIdentifier())
//                .setData(downstreamReqVO.getData());
//        sendDeviceMessage(message, device, result.getCode());
//
//        // 4. 如果不成功，抛出异常，提示用户
//        if (result.isError()) {
//            log.error("[otaUpgrade][设备({}) OTA 升级失败，请求参数：({})，响应结果：({})]",
//                    device.getDeviceKey(), reqDTO, result);
//            throw exception(DEVICE_DOWNSTREAM_FAILED, result.getMsg());
//        }
//        return message;
//    }

    private String getDeviceName(IotDeviceDO device, IotDeviceDO parentDevice) {
        return parentDevice != null ? parentDevice.getDeviceName() : device.getDeviceName();
    }

    private String getProductKey(IotDeviceDO device, IotDeviceDO parentDevice) {
        return parentDevice != null ? parentDevice.getProductKey() : device.getProductKey();
    }

}
