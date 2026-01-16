package cn.iocoder.yudao.module.iot.api.device;

import cn.iocoder.yudao.framework.common.enums.RpcConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceModbusConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceModbusPointDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceModbusConfigService;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceModbusPointService;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

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
    @Resource
    private IotProductService productService;
    @Resource
    private IotDeviceModbusConfigService modbusConfigService;
    @Resource
    private IotDeviceModbusPointService modbusPointService;

    @Override
    @PostMapping(RpcConstants.RPC_API_PREFIX + "/iot/device/auth")
    @PermitAll
    public CommonResult<Boolean> authDevice(@RequestBody IotDeviceAuthReqDTO authReqDTO) {
        return success(deviceService.authDevice(authReqDTO));
    }

    @Override
    @PostMapping(RpcConstants.RPC_API_PREFIX + "/iot/device/get") // 特殊：方便调用，暂时使用 POST，实际更推荐 GET
    @PermitAll
    public CommonResult<IotDeviceRespDTO> getDevice(@RequestBody IotDeviceGetReqDTO getReqDTO) {
        IotDeviceDO device = getReqDTO.getId() != null ? deviceService.getDeviceFromCache(getReqDTO.getId())
                : deviceService.getDeviceFromCache(getReqDTO.getProductKey(), getReqDTO.getDeviceName());
        return success(BeanUtils.toBean(device, IotDeviceRespDTO.class, deviceDTO -> {
            IotProductDO product = productService.getProductFromCache(deviceDTO.getProductId());
            if (product != null) {
                deviceDTO.setCodecType(product.getCodecType());
            }
        }));
    }

    @Override
    @PostMapping(RpcConstants.RPC_API_PREFIX + "/iot/modbus/enabled-configs")
    @PermitAll
    public CommonResult<List<IotModbusDeviceConfigRespDTO>> getEnabledModbusDeviceConfigs() {
        // 1. 获取所有启用的 Modbus 连接配置
        List<IotDeviceModbusConfigDO> configList = modbusConfigService.getEnabledModbusConfigList();
        if (configList.isEmpty()) {
            return success(new ArrayList<>());
        }

        // 2. 组装返回结果
        List<IotModbusDeviceConfigRespDTO> result = new ArrayList<>(configList.size());
        for (IotDeviceModbusConfigDO config : configList) {
            // 2.1 获取设备信息
            // TODO @AI：设备需要批量读取；（先暂时不处理）
            IotDeviceDO device = deviceService.getDeviceFromCache(config.getDeviceId());
            if (device == null) {
                continue;
            }

            // 2.2 获取启用的点位列表
            // TODO @AI：看看是不是批量读取；
            List<IotDeviceModbusPointDO> pointList = modbusPointService.getEnabledModbusPointListByDeviceId(config.getDeviceId());

            // 2.3 构建 DTO
            IotModbusDeviceConfigRespDTO dto = new IotModbusDeviceConfigRespDTO();
            dto.setDeviceId(config.getDeviceId());
            // TODO @AI：这个 productKey、deviceName 这个字段，要不要冗余到 IotDeviceModbusConfigDO 里面？（先暂时不处理）
            dto.setProductKey(device.getProductKey());
            dto.setDeviceName(device.getDeviceName());
            dto.setTenantId(device.getTenantId());
            // TODO @AI：看看 dto 的转换，能不能通过 beanutils copy
            dto.setIp(config.getIp());
            dto.setPort(config.getPort());
            dto.setSlaveId(config.getSlaveId());
            dto.setTimeout(config.getTimeout());
            dto.setRetryInterval(config.getRetryInterval());
            dto.setPoints(BeanUtils.toBean(pointList, IotModbusPointRespDTO.class));
            result.add(dto);
        }
        return success(result);
    }

}