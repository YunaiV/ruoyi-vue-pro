package cn.iocoder.yudao.module.iot.service.device;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData.IotDeviceDataReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDataDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.ThingModelMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodelfunction.IotThinkModelFunctionDO;
import cn.iocoder.yudao.module.iot.dal.redis.deviceData.DeviceDataRedisDAO;
import cn.iocoder.yudao.module.iot.enums.product.IotProductFunctionTypeEnum;
import cn.iocoder.yudao.module.iot.service.tdengine.IotThingModelMessageService;
import cn.iocoder.yudao.module.iot.service.thinkmodelfunction.IotThinkModelFunctionService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class IotDeviceDataServiceImpl implements IotDeviceDataService {

    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotThingModelMessageService thingModelMessageService;
    @Resource
    private IotThinkModelFunctionService thinkModelFunctionService;

    @Resource
    private DeviceDataRedisDAO deviceDataRedisDAO;

    @Override
    public void saveDeviceData(String productKey, String deviceName, String message) {
        // 1. 根据产品 key 和设备名称，获得设备信息
        IotDeviceDO device = deviceService.getDeviceByProductKeyAndDeviceName(productKey, deviceName);
        // 2. 解析消息，保存数据
        JSONObject jsonObject = new JSONObject(message);
        log.info("[saveDeviceData][productKey({}) deviceName({}) data({})]", productKey, deviceName, jsonObject);
        ThingModelMessage thingModelMessage = ThingModelMessage.builder()
                .id(jsonObject.getStr("id"))
                .sys(jsonObject.get("sys"))
                .method(jsonObject.getStr("method"))
                .params(jsonObject.get("params"))
                .time(jsonObject.getLong("time") == null ? System.currentTimeMillis() : jsonObject.getLong("time"))
                .productKey(productKey)
                .deviceName(deviceName)
                .deviceKey(device.getDeviceKey())
                .build();
        thingModelMessageService.saveThingModelMessage(device, thingModelMessage);
    }

    @Override
    public List<IotDeviceDataDO> getDevicePropertiesLatestData(@Valid IotDeviceDataReqVO deviceDataReqVO) {
        List<IotDeviceDataDO> list = new ArrayList<>();
        // 1. 获取设备信息
        IotDeviceDO device = deviceService.getDevice(deviceDataReqVO.getDeviceId());
        // 2. 获取设备属性最新数据
        List<IotThinkModelFunctionDO> thinkModelFunctionList = thinkModelFunctionService.getThinkModelFunctionListByProductKey(device.getProductKey());
        thinkModelFunctionList = thinkModelFunctionList.stream()
                .filter(function -> IotProductFunctionTypeEnum.PROPERTY.getType()
                        .equals(function.getType())).toList();

        // 3. 过滤标识符和属性名称
        if (deviceDataReqVO.getIdentifier() != null) {
            thinkModelFunctionList = thinkModelFunctionList.stream()
                    .filter(function -> function.getIdentifier().toLowerCase().contains(deviceDataReqVO.getIdentifier().toLowerCase()))
                    .toList();
        }
        if (deviceDataReqVO.getName() != null) {
            thinkModelFunctionList = thinkModelFunctionList.stream()
                    .filter(function -> function.getName().toLowerCase().contains(deviceDataReqVO.getName().toLowerCase()))
                    .toList();
        }
        // 4. 获取设备属性最新数据
        thinkModelFunctionList.forEach(function -> {
            IotDeviceDataDO deviceData = deviceDataRedisDAO.get(device.getProductKey(), device.getDeviceName(), function.getIdentifier());
            if (deviceData == null) {
                deviceData = new IotDeviceDataDO();
                deviceData.setProductKey(device.getProductKey());
                deviceData.setDeviceName(device.getDeviceName());
                deviceData.setIdentifier(function.getIdentifier());
                deviceData.setDeviceId(deviceDataReqVO.getDeviceId());
                deviceData.setThinkModelFunctionId(function.getId());
                deviceData.setName(function.getName());
                deviceData.setDataType(function.getProperty().getDataType().getType());
            }
            list.add(deviceData);
        });
        return list;
    }
}
