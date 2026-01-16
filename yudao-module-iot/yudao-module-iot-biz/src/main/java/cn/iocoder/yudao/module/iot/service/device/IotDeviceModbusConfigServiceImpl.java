package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusConfigPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceModbusConfigDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceModbusConfigMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.DEVICE_MODBUS_CONFIG_EXISTS;
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

    // TODO @AI：是不是搞成 save 接口？因为前端也不知道是 create 还是 update；
    @Override
    public Long createModbusConfig(IotDeviceModbusConfigSaveReqVO createReqVO) {
        // 1.1 校验设备存在
        deviceService.validateDeviceExists(createReqVO.getDeviceId());
        // 1.2 校验设备是否已有 Modbus 配置
        validateModbusConfigUnique(createReqVO.getDeviceId(), null);

        // 2. 插入
        IotDeviceModbusConfigDO modbusConfig = BeanUtils.toBean(createReqVO, IotDeviceModbusConfigDO.class);
        setDefaultValues(modbusConfig);
        modbusConfigMapper.insert(modbusConfig);
        return modbusConfig.getId();
    }

    @Override
    public void updateModbusConfig(IotDeviceModbusConfigSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateModbusConfigExists(updateReqVO.getId());
        // 1.2 校验设备存在
        deviceService.validateDeviceExists(updateReqVO.getDeviceId());
        // 1.3 校验唯一性
        validateModbusConfigUnique(updateReqVO.getDeviceId(), updateReqVO.getId());

        // 2. 更新
        IotDeviceModbusConfigDO updateObj = BeanUtils.toBean(updateReqVO, IotDeviceModbusConfigDO.class);
        modbusConfigMapper.updateById(updateObj);
    }

    @Override
    public void deleteModbusConfig(Long id) {
        // 校验存在
        validateModbusConfigExists(id);
        // 删除
        modbusConfigMapper.deleteById(id);
    }

    private void validateModbusConfigExists(Long id) {
        if (modbusConfigMapper.selectById(id) == null) {
            throw exception(DEVICE_MODBUS_CONFIG_NOT_EXISTS);
        }
    }

    private void validateModbusConfigUnique(Long deviceId, Long excludeId) {
        IotDeviceModbusConfigDO config = modbusConfigMapper.selectByDeviceId(deviceId);
        // TODO @AI：ObjUtil notequals
        if (config != null && !config.getId().equals(excludeId)) {
            throw exception(DEVICE_MODBUS_CONFIG_EXISTS);
        }
    }

    // TODO @AI：不要这个；前端都必须传递；
    private void setDefaultValues(IotDeviceModbusConfigDO config) {
        if (config.getPort() == null) {
            config.setPort(502);
        }
        if (config.getSlaveId() == null) {
            config.setSlaveId(1);
        }
        if (config.getTimeout() == null) {
            config.setTimeout(3000);
        }
        if (config.getRetryInterval() == null) {
            config.setRetryInterval(1000);
        }
    }

    @Override
    public IotDeviceModbusConfigDO getModbusConfig(Long id) {
        return modbusConfigMapper.selectById(id);
    }

    @Override
    public IotDeviceModbusConfigDO getModbusConfigByDeviceId(Long deviceId) {
        return modbusConfigMapper.selectByDeviceId(deviceId);
    }

    @Override
    public PageResult<IotDeviceModbusConfigDO> getModbusConfigPage(IotDeviceModbusConfigPageReqVO pageReqVO) {
        return modbusConfigMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotDeviceModbusConfigDO> getEnabledModbusConfigList() {
        return modbusConfigMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

}
