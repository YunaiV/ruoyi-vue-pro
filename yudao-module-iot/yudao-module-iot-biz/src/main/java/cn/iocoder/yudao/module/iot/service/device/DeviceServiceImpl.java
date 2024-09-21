package cn.iocoder.yudao.module.iot.service.device;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.IotDevicePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.IotDeviceSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.product.IotProductMapper;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.UUID;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 设备 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class DeviceServiceImpl implements IotDeviceService {

    @Resource
    private IotDeviceMapper deviceMapper;
    // TODO @haohao：不直接调用 productmapper，通过 productservice；每一个模型，不直接使用对方的
    @Resource
    private IotProductMapper productMapper;

    /**
     * 创建 IoT 设备
     *
     * @param createReqVO 创建请求 VO
     * @return 设备 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDevice(IotDeviceSaveReqVO createReqVO) {
        // 1.1 校验产品是否存在
        IotProductDO product = productMapper.selectById(createReqVO.getProductId());
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        // 1.2 校验设备名称在同一产品下是否唯一
        if (StrUtil.isBlank(createReqVO.getDeviceName())) {
            createReqVO.setDeviceName(generateUniqueDeviceName(product.getProductKey()));
        } else {
            validateDeviceNameUnique(product.getProductKey(), createReqVO.getDeviceName());
        }

        // 2.1 转换 VO 为 DO
        IotDeviceDO device = BeanUtils.toBean(createReqVO, IotDeviceDO.class)
                .setProductKey(product.getProductKey())
                .setDeviceType(product.getDeviceType());
        // 2.2 生成并设置必要的字段
        device.setDeviceKey(generateUniqueDeviceKey());
        device.setDeviceSecret(generateDeviceSecret());
        device.setMqttClientId(generateMqttClientId());
        device.setMqttUsername(generateMqttUsername(device.getDeviceName(), device.getProductKey()));
        device.setMqttPassword(generateMqttPassword());
        // 2.3 设置设备状态为未激活
        device.setStatus(IotDeviceStatusEnum.INACTIVE.getStatus());
        device.setStatusLastUpdateTime(LocalDateTime.now());
        // 2.4 插入到数据库
        deviceMapper.insert(device);
        return device.getId();
    }

    /**
     * 校验设备名称在同一产品下是否唯一
     *
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     */
    private void validateDeviceNameUnique(String productKey, String deviceName) {
        IotDeviceDO existingDevice = deviceMapper.selectByProductKeyAndDeviceName(productKey, deviceName);
        if (existingDevice != null) {
            throw exception(DEVICE_NAME_EXISTS);
        }
    }

    /**
     * 生成唯一的 deviceKey
     *
     * @return 生成的 deviceKey
     */
    private String generateUniqueDeviceKey() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成 deviceSecret
     *
     * @return 生成的 deviceSecret
     */
    private String generateDeviceSecret() {
        // TODO @haohao：return IdUtil.fastSimpleUUID()
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成 MQTT Client ID
     *
     * @return 生成的 MQTT Client ID
     */
    private String generateMqttClientId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成 MQTT Username
     *
     * @param deviceName 设备名称
     * @param productKey 产品 Key
     * @return 生成的 MQTT Username
     */
    private String generateMqttUsername(String deviceName, String productKey) {
        return deviceName + "&" + productKey;
    }

    /**
     * 生成 MQTT Password
     *
     * @return 生成的 MQTT Password
     */
    private String generateMqttPassword() {
        // TODO @haohao：【后续优化】在实际应用中，建议使用更安全的方法生成 MQTT Password，如加密或哈希
        return UUID.randomUUID().toString();
    }

    /**
     * 生成唯一的 DeviceName
     *
     * @param productKey 产品标识
     * @return 生成的唯一 DeviceName
     */
    private String generateUniqueDeviceName(String productKey) {
        // TODO @haohao：业务逻辑里，尽量避免 while true。万一 bug = =；虽然这个不会哈。我先改了下
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            String deviceName = IdUtil.fastSimpleUUID().substring(0, 20);
            if (deviceMapper.selectByProductKeyAndDeviceName(productKey, deviceName) != null) {
                return deviceName;
            }
        }
        throw new IllegalArgumentException("生成 DeviceName 失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDevice(IotDeviceSaveReqVO updateReqVO) {
        // 校验存在
        IotDeviceDO existingDevice = validateDeviceExists(updateReqVO.getId());

        // 设备名称 和 产品 ID 不能修改
        // TODO @haohao：这种，直接设置为 null 就不会更新了。忽略前端的传参
        if (updateReqVO.getDeviceName() != null && !updateReqVO.getDeviceName().equals(existingDevice.getDeviceName())) {
            throw exception(DEVICE_NAME_CANNOT_BE_MODIFIED);
        }
        if (updateReqVO.getProductId() != null && !updateReqVO.getProductId().equals(existingDevice.getProductId())) {
            throw exception(DEVICE_PRODUCT_CANNOT_BE_MODIFIED);
        }

        // 更新 DO 对象
        IotDeviceDO updateObj = BeanUtils.toBean(updateReqVO, IotDeviceDO.class);

        // 更新到数据库
        deviceMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDevice(Long id) {
        // 1.1 校验存在
        IotDeviceDO device = validateDeviceExists(id);
        // 1.2 如果是网关设备，检查是否有子设备
        if (device.getGatewayId() != null && deviceMapper.selectCountByGatewayId(id) > 0) {
            throw exception(DEVICE_HAS_CHILDREN);
        }

        // 2. 删除设备
        deviceMapper.deleteById(id);
    }

    /**
     * 校验设备是否存在
     *
     * @param id 设备 ID
     * @return 设备对象
     */
    private IotDeviceDO validateDeviceExists(Long id) {
        IotDeviceDO device = deviceMapper.selectById(id);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        return device;
    }

    @Override
    public IotDeviceDO getDevice(Long id) {
        IotDeviceDO device = deviceMapper.selectById(id);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        return device;
    }

    @Override
    public PageResult<IotDeviceDO> getDevicePage(IotDevicePageReqVO pageReqVO) {
        return deviceMapper.selectPage(pageReqVO);
    }

    @Override
    public void updateDeviceStatus(Long id, Integer status) {
        // 校验存在
        validateDeviceExists(id);

        // TODO @haohao：这个可以直接用 swagger 注解哈
        // 校验状态是否合法
        if (!IotDeviceStatusEnum.isValidStatus(status)) {
            throw exception(DEVICE_INVALID_DEVICE_STATUS);
        }

        // 更新状态和更新时间
        IotDeviceDO updateObj = new IotDeviceDO().setId(id).setStatus(status)
                .setStatusLastUpdateTime(LocalDateTime.now());
        deviceMapper.updateById(updateObj);
    }

}