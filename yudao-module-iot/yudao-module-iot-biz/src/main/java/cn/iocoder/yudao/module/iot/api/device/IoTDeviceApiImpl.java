package cn.iocoder.yudao.module.iot.api.device;

import cn.hutool.core.collection.CollUtil;
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

import java.math.BigDecimal;
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
        // TODO @芋艿：临时 mock 数据，用于测试 ModbusTcpSlaveSimulator
        if (true) {
            return success(buildMockModbusConfigs());
        }

        // 1. 获取所有启用的 Modbus 连接配置
        List<IotDeviceModbusConfigDO> configList = modbusConfigService.getEnabledDeviceModbusConfigList();
        if (CollUtil.isEmpty(configList)) {
            return success(new ArrayList<>());
        }

        // 2. 组装返回结果
        Set<Long> deviceIds = convertSet(configList, IotDeviceModbusConfigDO::getDeviceId);
        Map<Long, IotDeviceDO> deviceMap = deviceService.getDeviceMap(deviceIds);
        Map<Long, List<IotDeviceModbusPointDO>> pointMap = modbusPointService.getEnabledDeviceModbusPointMapByDeviceIds(deviceIds);
        List<IotModbusDeviceConfigRespDTO> result = new ArrayList<>(configList.size());
        for (IotDeviceModbusConfigDO config : configList) {
            // 3.1 获取设备信息
            IotDeviceDO device = deviceMap.get(config.getDeviceId());
            if (device == null) {
                continue;
            }
            // 3.2 获取启用的点位列表
            List<IotDeviceModbusPointDO> pointList = pointMap.get(config.getDeviceId());
            if (CollUtil.isEmpty(pointList)) {
                continue;
            }
            // 3.3 构建 IotModbusDeviceConfigRespDTO 对象
            IotModbusDeviceConfigRespDTO configDTO = BeanUtils.toBean(config, IotModbusDeviceConfigRespDTO.class, o ->
                    o.setProductKey(device.getProductKey()).setDeviceName(device.getDeviceName())
                            .setPoints(BeanUtils.toBean(pointList, IotModbusPointRespDTO.class)));
            result.add(configDTO);
        }
        return success(result);
    }

    /**
     * 构建 Mock Modbus 配置，对接 ModbusTcpSlaveSimulator
     *
     * 设备：productKey=4aymZgOTOOCrDKRT, deviceName=small
     * 物模型字段：width, height, oneThree
     */
    private List<IotModbusDeviceConfigRespDTO> buildMockModbusConfigs() {
        List<IotModbusDeviceConfigRespDTO> configs = new ArrayList<>();

        // 设备配置
        IotModbusDeviceConfigRespDTO config = new IotModbusDeviceConfigRespDTO();
        config.setDeviceId(1L);
        config.setProductKey("4aymZgOTOOCrDKRT");
        config.setDeviceName("small");
        config.setIp("127.0.0.1");
        config.setPort(5020); // 对应 ModbusTcpSlaveSimulator 的端口
        config.setSlaveId(1);
        config.setTimeout(3000);
        config.setRetryInterval(5000);

        // 点位配置（对应物模型字段：width, height, oneThree）
        List<IotModbusPointRespDTO> points = new ArrayList<>();

        // 点位 1：width - 读取保持寄存器地址 0（功能码 03）
        IotModbusPointRespDTO point1 = new IotModbusPointRespDTO();
        point1.setId(1L);
        point1.setIdentifier("width");
        point1.setName("宽度");
        point1.setFunctionCode(3); // 读保持寄存器
        point1.setRegisterAddress(0);
        point1.setRegisterCount(1);
        point1.setByteOrder("AB");
        point1.setRawDataType("INT16");
        point1.setScale(BigDecimal.ONE);
        point1.setPollInterval(3000); // 3 秒轮询
        points.add(point1);

        // 点位 2：height - 读取保持寄存器地址 1（功能码 03）
        IotModbusPointRespDTO point2 = new IotModbusPointRespDTO();
        point2.setId(2L);
        point2.setIdentifier("height");
        point2.setName("高度");
        point2.setFunctionCode(3); // 读保持寄存器
        point2.setRegisterAddress(1);
        point2.setRegisterCount(1);
        point2.setByteOrder("AB");
        point2.setRawDataType("INT16");
        point2.setScale(BigDecimal.ONE);
        point2.setPollInterval(3000); // 3 秒轮询
        points.add(point2);

        // 点位 3：oneThree - 读取保持寄存器地址 2（功能码 03）
        IotModbusPointRespDTO point3 = new IotModbusPointRespDTO();
        point3.setId(3L);
        point3.setIdentifier("oneThree");
        point3.setName("一三");
        point3.setFunctionCode(3); // 读保持寄存器
        point3.setRegisterAddress(2);
        point3.setRegisterCount(1);
        point3.setByteOrder("AB");
        point3.setRawDataType("INT16");
        point3.setScale(BigDecimal.ONE);
        point3.setPollInterval(3000); // 3 秒轮询
        points.add(point3);

        config.setPoints(points);
        configs.add(config);
        return configs;
    }

}