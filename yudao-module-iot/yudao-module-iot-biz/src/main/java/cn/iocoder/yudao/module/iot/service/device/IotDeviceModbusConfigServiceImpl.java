package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceModbusConfigDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceModbusConfigMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.DEVICE_MODBUS_CONFIG_NOT_EXISTS;

/**
 * IoT 设备 Modbus 连接配置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class IotDeviceModbusConfigServiceImpl implements IotDeviceModbusConfigService {

    @Resource
    private IotDeviceModbusConfigMapper modbusConfigMapper;

    @Resource
    private IotDeviceService deviceService;

    @Override
    public void saveDeviceModbusConfig(IotDeviceModbusConfigSaveReqVO saveReqVO) {
        // 1. 校验设备存在
        deviceService.validateDeviceExists(saveReqVO.getDeviceId());

        // 2. 根据数据库中是否已有配置，决定是新增还是更新
        IotDeviceModbusConfigDO existConfig = modbusConfigMapper.selectByDeviceId(saveReqVO.getDeviceId());
        if (existConfig == null) {
            IotDeviceModbusConfigDO modbusConfig = BeanUtils.toBean(saveReqVO, IotDeviceModbusConfigDO.class);
            modbusConfigMapper.insert(modbusConfig);
        } else {
            IotDeviceModbusConfigDO updateObj = BeanUtils.toBean(saveReqVO, IotDeviceModbusConfigDO.class,
                    o -> o.setId(existConfig.getId()));
            modbusConfigMapper.updateById(updateObj);
        }
    }

    @Override
    public void deleteDeviceModbusConfig(Long id) {
        // 校验存在
        validateDeviceModbusConfigExists(id);
        // 删除
        modbusConfigMapper.deleteById(id);
    }

    private void validateDeviceModbusConfigExists(Long id) {
        if (modbusConfigMapper.selectById(id) == null) {
            throw exception(DEVICE_MODBUS_CONFIG_NOT_EXISTS);
        }
    }

    @Override
    public IotDeviceModbusConfigDO getDeviceModbusConfig(Long id) {
        return modbusConfigMapper.selectById(id);
    }

    @Override
    public IotDeviceModbusConfigDO getDeviceModbusConfigByDeviceId(Long deviceId) {
        return modbusConfigMapper.selectByDeviceId(deviceId);
    }

    @Override
    public List<IotDeviceModbusConfigDO> getEnabledDeviceModbusConfigList() {
        return modbusConfigMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

}
