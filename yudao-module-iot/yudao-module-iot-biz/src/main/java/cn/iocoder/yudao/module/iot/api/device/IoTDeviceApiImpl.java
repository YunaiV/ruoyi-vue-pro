package cn.iocoder.yudao.module.iot.api.device;

import cn.iocoder.yudao.framework.common.enums.RpcConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceInfoReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceInfoRespDTO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * IoT 设备 API 实现类
 *
 * @author haohao
 */
@RestController
@Validated
@Primary // 保证优先匹配，因为 yudao-iot-gateway 也有 IotDeviceCommonApi 的实现，并且也可能会被 biz 引入
public class IoTDeviceApiImpl implements IotDeviceCommonApi {

    @Resource
    private IotDeviceService deviceService;

    @Override
    @PostMapping(RpcConstants.RPC_API_PREFIX + "/iot/device/auth")
    @PermitAll
    public CommonResult<Boolean> authDevice(@RequestBody IotDeviceAuthReqDTO authReqDTO) {
        return success(deviceService.authDevice(authReqDTO));
    }

    @Override
    @PostMapping(RpcConstants.RPC_API_PREFIX + "/iot/device/info")
    @PermitAll
    public CommonResult<IotDeviceInfoRespDTO> getDeviceInfo(@RequestBody IotDeviceInfoReqDTO infoReqDTO) {
        IotDeviceDO device = deviceService.getDeviceByProductKeyAndDeviceNameFromCache(
                infoReqDTO.getProductKey(), infoReqDTO.getDeviceName());

        if (device == null) {
            return success(null);
        }

        IotDeviceInfoRespDTO respDTO = new IotDeviceInfoRespDTO();
        respDTO.setDeviceId(device.getId());
        respDTO.setProductKey(device.getProductKey());
        respDTO.setDeviceName(device.getDeviceName());
        respDTO.setDeviceKey(device.getDeviceKey());
        respDTO.setTenantId(device.getTenantId());

        return success(respDTO);
    }

}