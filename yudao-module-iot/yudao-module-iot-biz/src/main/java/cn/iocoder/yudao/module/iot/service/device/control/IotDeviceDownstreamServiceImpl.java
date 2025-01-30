package cn.iocoder.yudao.module.iot.service.device.control;

import cn.iocoder.yudao.module.iot.controller.admin.device.vo.control.IotDeviceSimulationUpstreamReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceMessageIdentifierEnum;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceMessageTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * 设备下行 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotDeviceDownstreamServiceImpl implements IotDeviceDownstreamService {

    @Resource
    private IotDeviceService deviceService;

    @Override
    public void simulationDeviceDownstream(IotDeviceSimulationUpstreamReqVO downstreamReqVO) {
        // 校验设备是否存在
        IotDeviceDO device = deviceService.validateDeviceExists(downstreamReqVO.getId());
        // TODO 芋艿：父设备的处理
        IotDeviceDO parentDevice = null;

        // 服务调用
        if (Objects.equals(downstreamReqVO.getType(), IotDeviceMessageTypeEnum.SERVICE.getType())) {
            invokeDeviceService(downstreamReqVO, device, parentDevice);
            return;
        }
        // 属性相关
        if (Objects.equals(downstreamReqVO.getType(), IotDeviceMessageTypeEnum.PROPERTY.getType()))
            // 属性设置
            if (Objects.equals(downstreamReqVO.getIdentifier(), IotDeviceMessageIdentifierEnum.PROPERTY_SET.getIdentifier())) {
                setDeviceProperty(downstreamReqVO, device, parentDevice);
                return;
            }
        // 属性设置
        if (Objects.equals(downstreamReqVO.getIdentifier(), IotDeviceMessageIdentifierEnum.PROPERTY_GET.getIdentifier())) {
            getDeviceProperty(downstreamReqVO, device, parentDevice);
            return;
        }
        // TODO 芋艿：ota 升级
        // TODO 芋艿：配置下发
    }

    private void invokeDeviceService(IotDeviceSimulationUpstreamReqVO downstreamReqVO,
                                     IotDeviceDO device, IotDeviceDO parentDevice) {
        // 校验服务是否存在
        // TODO 芋艿：这里需要校验服务是否存在
        // 调用服务
        // TODO 芋艿：这里需要调用服务
    }

    private void setDeviceProperty(IotDeviceSimulationUpstreamReqVO downstreamReqVO,
                                   IotDeviceDO device, IotDeviceDO parentDevice) {
        // TODO 芋艿：这里需要设置设备属性
    }

    private void getDeviceProperty(IotDeviceSimulationUpstreamReqVO downstreamReqVO,
                                   IotDeviceDO device, IotDeviceDO parentDevice) {
        // TODO 芋艿：这里需要获取设备属性
    }

}
