package cn.iocoder.yudao.module.iot.service.device;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.opcua.IotDeviceOpcUaConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaDeviceConfigListReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceOpcUaConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceOpcUaConfigMapper;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 设备 OPC UA 连接配置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class IotDeviceOpcUaConfigServiceImpl implements IotDeviceOpcUaConfigService {

    @Resource
    private IotDeviceOpcUaConfigMapper opcUaConfigMapper;

    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotProductService productService;

    @Override
    public void saveDeviceOpcUaConfig(IotDeviceOpcUaConfigSaveReqVO saveReqVO) {
        // 1.1 校验设备存在，且产品协议是 OPC UA Client
        IotDeviceDO device = deviceService.validateDeviceExists(saveReqVO.getDeviceId());
        IotProductDO product = productService.getProduct(device.getProductId());
        if (product == null || ObjUtil.notEqual(IotProtocolTypeEnum.OPC_UA_CLIENT.getType(), product.getProtocolType())) {
            throw exception(DEVICE_OPC_UA_CONFIG_PROTOCOL_INVALID);
        }
        // 1.2 端点格式（不允许首尾空白，网关按端点地址完全匹配凭据）、安全策略与模式、认证方式（只支持用户名认证）的校验，
        //     见 IotDeviceOpcUaConfigSaveReqVO

        // 2. 每台设备只有一份未删除的配置（应用层唯一，与 Modbus 一致不加锁）：已有则更新，没有则新增
        //    并发保存产生的重复记录不会导致报错：查询取编号最小的一条，网关配置接口按设备去重
        IotDeviceOpcUaConfigDO existConfig = opcUaConfigMapper.selectByDeviceId(saveReqVO.getDeviceId());
        if (existConfig == null) {
            opcUaConfigMapper.insert(BeanUtils.toBean(saveReqVO, IotDeviceOpcUaConfigDO.class));
        } else {
            opcUaConfigMapper.updateById(BeanUtils.toBean(saveReqVO, IotDeviceOpcUaConfigDO.class,
                    o -> o.setId(existConfig.getId())));
        }
    }

    @Override
    public IotDeviceOpcUaConfigDO getDeviceOpcUaConfig(Long id) {
        return opcUaConfigMapper.selectById(id);
    }

    @Override
    public IotDeviceOpcUaConfigDO getDeviceOpcUaConfigByDeviceId(Long deviceId) {
        return opcUaConfigMapper.selectByDeviceId(deviceId);
    }

    @Override
    public List<IotDeviceOpcUaConfigDO> getDeviceOpcUaConfigList(IotOpcUaDeviceConfigListReqDTO listReqDTO) {
        return opcUaConfigMapper.selectList(listReqDTO);
    }

}
