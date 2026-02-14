package cn.iocoder.yudao.module.iot.service.device;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigListReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceModbusConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceModbusConfigMapper;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

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
    @Resource
    private IotProductService productService;

    @Override
    public void saveDeviceModbusConfig(IotDeviceModbusConfigSaveReqVO saveReqVO) {
        // 1.1 校验设备存在
        IotDeviceDO device = deviceService.validateDeviceExists(saveReqVO.getDeviceId());
        // 1.2 根据产品 protocolType 条件校验
        IotProductDO product = productService.getProduct(device.getProductId());
        Assert.notNull(product, "产品不存在");
        validateModbusConfigByProtocolType(saveReqVO, product.getProtocolType());

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
    public IotDeviceModbusConfigDO getDeviceModbusConfig(Long id) {
        return modbusConfigMapper.selectById(id);
    }

    @Override
    public IotDeviceModbusConfigDO getDeviceModbusConfigByDeviceId(Long deviceId) {
        return modbusConfigMapper.selectByDeviceId(deviceId);
    }

    @Override
    public List<IotDeviceModbusConfigDO> getDeviceModbusConfigList(IotModbusDeviceConfigListReqDTO listReqDTO) {
        return modbusConfigMapper.selectList(listReqDTO);
    }

    private void validateModbusConfigByProtocolType(IotDeviceModbusConfigSaveReqVO saveReqVO, String protocolType) {
        IotProtocolTypeEnum protocolTypeEnum = IotProtocolTypeEnum.of(protocolType);
        if (protocolTypeEnum == null) {
            return;
        }
        if (protocolTypeEnum == IotProtocolTypeEnum.MODBUS_TCP_CLIENT) {
            Assert.isTrue(StrUtil.isNotEmpty(saveReqVO.getIp()), "Client 模式下，IP 地址不能为空");
            Assert.notNull(saveReqVO.getPort(), "Client 模式下，端口不能为空");
            Assert.notNull(saveReqVO.getTimeout(), "Client 模式下，连接超时时间不能为空");
            Assert.notNull(saveReqVO.getRetryInterval(), "Client 模式下，重试间隔不能为空");
        } else if (protocolTypeEnum == IotProtocolTypeEnum.MODBUS_TCP_SERVER) {
            Assert.notNull(saveReqVO.getMode(), "Server 模式下，工作模式不能为空");
            Assert.notNull(saveReqVO.getFrameFormat(), "Server 模式下，数据帧格式不能为空");
        }
    }

}
