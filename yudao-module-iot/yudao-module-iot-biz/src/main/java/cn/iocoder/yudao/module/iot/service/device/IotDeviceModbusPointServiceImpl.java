package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusPointPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusPointSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceModbusPointDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceModbusPointMapper;
import cn.iocoder.yudao.module.iot.service.thingmodel.IotThingModelService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 设备 Modbus 点位配置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class IotDeviceModbusPointServiceImpl implements IotDeviceModbusPointService {

    @Resource
    private IotDeviceModbusPointMapper modbusPointMapper;

    @Resource
    private IotDeviceService deviceService;

    @Resource
    private IotThingModelService thingModelService;

    @Override
    public Long createModbusPoint(IotDeviceModbusPointSaveReqVO createReqVO) {
        // 1.1 校验设备存在
        deviceService.validateDeviceExists(createReqVO.getDeviceId());
        // 1.2 校验物模型属性存在
        IotThingModelDO thingModel = validateThingModelExists(createReqVO.getThingModelId());
        // 1.3 校验同一设备下点位唯一性（基于 identifier）
        validateModbusPointUnique(createReqVO.getDeviceId(), thingModel.getIdentifier(), null);

        // 2. 插入
        IotDeviceModbusPointDO modbusPoint = BeanUtils.toBean(createReqVO, IotDeviceModbusPointDO.class,
                o -> o.setIdentifier(thingModel.getIdentifier()).setName(thingModel.getName()));
        modbusPointMapper.insert(modbusPoint);
        return modbusPoint.getId();
    }

    @Override
    public void updateModbusPoint(IotDeviceModbusPointSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateModbusPointExists(updateReqVO.getId());
        // 1.2 校验设备存在
        deviceService.validateDeviceExists(updateReqVO.getDeviceId());
        // 1.3 校验物模型属性存在
        IotThingModelDO thingModel = validateThingModelExists(updateReqVO.getThingModelId());
        // 1.4 校验同一设备下点位唯一性
        validateModbusPointUnique(updateReqVO.getDeviceId(), thingModel.getIdentifier(), updateReqVO.getId());

        // 2. 更新
        IotDeviceModbusPointDO updateObj = BeanUtils.toBean(updateReqVO, IotDeviceModbusPointDO.class);
        // TODO @AI：这块
        modbusPointMapper.updateById(updateObj);
    }

    private IotThingModelDO validateThingModelExists(Long id) {
        IotThingModelDO thingModel = thingModelService.getThingModel(id);
        if (thingModel == null) {
            throw exception(THING_MODEL_NOT_EXISTS);
        }
        return thingModel;
    }

    @Override
    public void deleteModbusPoint(Long id) {
        // 校验存在
        validateModbusPointExists(id);
        // 删除
        modbusPointMapper.deleteById(id);
    }

    private IotDeviceModbusPointDO validateModbusPointExists(Long id) {
        IotDeviceModbusPointDO point = modbusPointMapper.selectById(id);
        if (point == null) {
            throw exception(DEVICE_MODBUS_POINT_NOT_EXISTS);
        }
        return point;
    }

    private void validateModbusPointUnique(Long deviceId, String identifier, Long excludeId) {
        IotDeviceModbusPointDO point = modbusPointMapper.selectByDeviceIdAndIdentifier(deviceId, identifier);
        // TODO @AI：ObjUtil notequals；
        if (point != null && !point.getId().equals(excludeId)) {
            throw exception(DEVICE_MODBUS_POINT_EXISTS);
        }
    }

    // TODO @AI：这块
    private void setDefaultValues(IotDeviceModbusPointDO point) {
        if (point.getRegisterCount() == null) {
            point.setRegisterCount(1);
        }
        if (point.getScale() == null) {
            point.setScale(BigDecimal.ONE);
        }
        if (point.getPollInterval() == null) {
            point.setPollInterval(5000);
        }
    }

    @Override
    public IotDeviceModbusPointDO getModbusPoint(Long id) {
        return modbusPointMapper.selectById(id);
    }

    @Override
    public PageResult<IotDeviceModbusPointDO> getModbusPointPage(IotDeviceModbusPointPageReqVO pageReqVO) {
        return modbusPointMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotDeviceModbusPointDO> getModbusPointListByDeviceId(Long deviceId) {
        return modbusPointMapper.selectListByDeviceId(deviceId);
    }

    @Override
    public List<IotDeviceModbusPointDO> getEnabledModbusPointListByDeviceId(Long deviceId) {
        return modbusPointMapper.selectListByDeviceIdAndStatus(deviceId, CommonStatusEnum.ENABLE.getStatus());
    }

}
