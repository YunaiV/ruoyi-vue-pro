package cn.iocoder.yudao.module.iot.api.device;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.RpcConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.*;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterRespDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotSubDeviceRegisterRespDTO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceModbusConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceModbusPointDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceModbusConfigService;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceModbusPointService;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

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
    @Lazy // 延迟加载，解决循环依赖
    private IotDeviceModbusConfigService modbusConfigService;
    @Resource
    @Lazy // 延迟加载，解决循环依赖
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
                deviceDTO.setProtocolType(product.getProtocolType()).setSerializeType(product.getSerializeType());
            }
        }));
    }

    @Override
    @PostMapping(RpcConstants.RPC_API_PREFIX + "/iot/modbus/config-list")
    @PermitAll
    @TenantIgnore
    public CommonResult<List<IotModbusDeviceConfigRespDTO>> getModbusDeviceConfigList(
            @RequestBody IotModbusDeviceConfigListReqDTO listReqDTO) {
        // 1. 获取 Modbus 连接配置
        List<IotDeviceModbusConfigDO> configList = modbusConfigService.getDeviceModbusConfigList(listReqDTO);
        if (CollUtil.isEmpty(configList)) {
            return success(new ArrayList<>());
        }

        // 2. 组装返回结果
        Set<Long> deviceIds = convertSet(configList, IotDeviceModbusConfigDO::getDeviceId);
        Map<Long, IotDeviceDO> deviceMap = deviceService.getDeviceMap(deviceIds);
        Map<Long, List<IotDeviceModbusPointDO>> pointMap = modbusPointService.getEnabledDeviceModbusPointMapByDeviceIds(deviceIds);
        Map<Long, IotProductDO> productMap = productService.getProductMap(convertSet(deviceMap.values(), IotDeviceDO::getProductId));
        List<IotModbusDeviceConfigRespDTO> result = new ArrayList<>(configList.size());
        for (IotDeviceModbusConfigDO config : configList) {
            // 3.1 获取设备信息
            IotDeviceDO device = deviceMap.get(config.getDeviceId());
            if (device == null) {
                continue;
            }
            // 3.2 按 protocolType 筛选（如果非空）
            if (StrUtil.isNotEmpty(listReqDTO.getProtocolType())) {
                IotProductDO product = productMap.get(device.getProductId());
                if (product == null || ObjUtil.notEqual(listReqDTO.getProtocolType(), product.getProtocolType())) {
                    continue;
                }
            }
            // 3.3 获取启用的点位列表
            List<IotDeviceModbusPointDO> pointList = pointMap.get(config.getDeviceId());
            if (CollUtil.isEmpty(pointList)) {
                continue;
            }

            // 3.4 构建 IotModbusDeviceConfigRespDTO 对象
            IotModbusDeviceConfigRespDTO configDTO = BeanUtils.toBean(config, IotModbusDeviceConfigRespDTO.class, o ->
                    o.setProductKey(device.getProductKey()).setDeviceName(device.getDeviceName())
                            .setPoints(BeanUtils.toBean(pointList, IotModbusPointRespDTO.class)));
            result.add(configDTO);
        }
        return success(result);
    }

    @Override
    @PostMapping(RpcConstants.RPC_API_PREFIX + "/iot/device/register")
    @PermitAll
    public CommonResult<IotDeviceRegisterRespDTO> registerDevice(@RequestBody IotDeviceRegisterReqDTO reqDTO) {
        return success(deviceService.registerDevice(reqDTO));
    }

    @Override
    @PostMapping(RpcConstants.RPC_API_PREFIX + "/iot/device/register-sub")
    @PermitAll
    public CommonResult<List<IotSubDeviceRegisterRespDTO>> registerSubDevices(@RequestBody IotSubDeviceRegisterFullReqDTO reqDTO) {
        return success(deviceService.registerSubDevices(reqDTO));
    }

}