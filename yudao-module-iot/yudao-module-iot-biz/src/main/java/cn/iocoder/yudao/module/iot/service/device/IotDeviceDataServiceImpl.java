package cn.iocoder.yudao.module.iot.service.device;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData.IotDeviceDataPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDataDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.SelectVisualDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.ThingModelMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotProductThingModelDO;
import cn.iocoder.yudao.module.iot.dal.redis.deviceData.DeviceDataRedisDAO;
import cn.iocoder.yudao.module.iot.dal.tdengine.TdEngineDMLMapper;
import cn.iocoder.yudao.module.iot.enums.IotConstants;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotProductThingModelTypeEnum;
import cn.iocoder.yudao.module.iot.service.tdengine.IotThingModelMessageService;
import cn.iocoder.yudao.module.iot.service.thingmodel.IotProductThingModelService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;

@Slf4j
@Service
public class IotDeviceDataServiceImpl implements IotDeviceDataService {

    @Value("${spring.datasource.dynamic.datasource.tdengine.url}")
    private String url;

    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotThingModelMessageService thingModelMessageService;
    @Resource
    private IotProductThingModelService thingModelService;
    @Resource
    private TdEngineDMLMapper tdEngineDMLMapper;

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
    public List<IotDeviceDataDO> getLatestDeviceProperties(@Valid IotDeviceDataPageReqVO deviceDataReqVO) {
        List<IotDeviceDataDO> list = new ArrayList<>();
        // 1. 获取设备信息
        IotDeviceDO device = deviceService.getDevice(deviceDataReqVO.getDeviceId());
        // 2. 获取设备属性最新数据
        List<IotProductThingModelDO> thingModelList = thingModelService.getProductThingModelListByProductKey(device.getProductKey());
        thingModelList = filterList(thingModelList, thingModel -> IotProductThingModelTypeEnum.PROPERTY.getType()
                .equals(thingModel.getType()));

        // 3. 过滤标识符和属性名称
        if (deviceDataReqVO.getIdentifier() != null) {
            thingModelList = filterList(thingModelList, thingModel -> thingModel.getIdentifier()
                    .toLowerCase().contains(deviceDataReqVO.getIdentifier().toLowerCase()));
        }
        if (deviceDataReqVO.getName() != null) {
            thingModelList = filterList(thingModelList, thingModel -> thingModel.getName()
                    .toLowerCase().contains(deviceDataReqVO.getName().toLowerCase()));
        }
        // 4. 获取设备属性最新数据
        thingModelList.forEach(thingModel -> {
            IotDeviceDataDO deviceData = deviceDataRedisDAO.get(device.getProductKey(), device.getDeviceName(), thingModel.getIdentifier());
            if (deviceData == null) {
                deviceData = new IotDeviceDataDO();
                deviceData.setProductKey(device.getProductKey());
                deviceData.setDeviceName(device.getDeviceName());
                deviceData.setIdentifier(thingModel.getIdentifier());
                deviceData.setDeviceId(deviceDataReqVO.getDeviceId());
                deviceData.setThingModelId(thingModel.getId());
                deviceData.setName(thingModel.getName());
                deviceData.setDataType(thingModel.getProperty().getDataType());
            }
            list.add(deviceData);
        });
        return list;
    }

    @Override
    public PageResult<Map<String, Object>> getHistoryDeviceProperties(IotDeviceDataPageReqVO deviceDataReqVO) {
        PageResult<Map<String, Object>> pageResult = new PageResult<>();
        // 1. 获取设备信息
        IotDeviceDO device = deviceService.getDevice(deviceDataReqVO.getDeviceId());
        // 2. 获取设备属性历史数据
        SelectVisualDO selectVisualDO = new SelectVisualDO();
        selectVisualDO.setDataBaseName(getDatabaseName());
        selectVisualDO.setTableName(getDeviceTableName(device.getProductKey(), device.getDeviceName()));
        selectVisualDO.setFieldName(deviceDataReqVO.getIdentifier());
        selectVisualDO.setStartTime(DateUtil.date(deviceDataReqVO.getTimes()[0].atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()).getTime());
        selectVisualDO.setEndTime(DateUtil.date(deviceDataReqVO.getTimes()[1].atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()).getTime());
        Map<String, Object> params = new HashMap<>();
        params.put("rows", deviceDataReqVO.getPageSize());
        params.put("page", (deviceDataReqVO.getPageNo() - 1) * deviceDataReqVO.getPageSize());
        selectVisualDO.setParams(params);
        pageResult.setList(tdEngineDMLMapper.selectHistoryDataList(selectVisualDO));
        pageResult.setTotal(tdEngineDMLMapper.selectHistoryCount(selectVisualDO));
        return pageResult;
    }

    private String getDatabaseName() {
        return StrUtil.subAfter(url, "/", true);
    }

    private static String getDeviceTableName(String productKey, String deviceName) {
        return String.format(IotConstants.DEVICE_TABLE_NAME_FORMAT, productKey, deviceName);
    }

}