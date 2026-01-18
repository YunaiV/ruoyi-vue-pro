package cn.iocoder.yudao.module.iot.service.device;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.*;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceGroupDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants;
import cn.iocoder.yudao.module.iot.enums.product.IotProductDeviceTypeEnum;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import jakarta.annotation.Resource;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 设备 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotDeviceServiceImpl implements IotDeviceService {

    @Resource
    private IotDeviceMapper deviceMapper;

    @Resource
    @Lazy  // 延迟加载，解决循环依赖
    private IotProductService productService;
    @Resource
    @Lazy // 延迟加载，解决循环依赖
    private IotDeviceGroupService deviceGroupService;

    @Override
    public Long createDevice(IotDeviceSaveReqVO createReqVO) {
        // 1.1 校验产品是否存在
        IotProductDO product = productService.getProduct(createReqVO.getProductId());
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        // 1.2 统一校验
        validateCreateDeviceParam(product.getProductKey(), createReqVO.getDeviceName(),
                createReqVO.getGatewayId(), product);
        // 1.3 校验分组存在
        deviceGroupService.validateDeviceGroupExists(createReqVO.getGroupIds());
        // 1.4 校验设备序列号全局唯一
        validateSerialNumberUnique(createReqVO.getSerialNumber(), null);

        // 2. 插入到数据库
        IotDeviceDO device = BeanUtils.toBean(createReqVO, IotDeviceDO.class);
        initDevice(device, product);
        deviceMapper.insert(device);
        return device.getId();
    }

    private void validateCreateDeviceParam(String productKey, String deviceName,
                                           Long gatewayId, IotProductDO product) {
        // 校验设备名称在同一产品下是否唯一
        TenantUtils.executeIgnore(() -> {
            if (deviceMapper.selectByProductKeyAndDeviceName(productKey, deviceName) != null) {
                throw exception(DEVICE_NAME_EXISTS);
            }
        });
        // 校验父设备是否为合法网关
        if (IotProductDeviceTypeEnum.isGatewaySub(product.getDeviceType())
                && gatewayId != null) {
            validateGatewayDeviceExists(gatewayId);
        }
    }

    /**
     * 校验设备序列号全局唯一性
     *
     * @param serialNumber 设备序列号
     * @param excludeId 排除的设备编号（用于更新时排除自身）
     */
    private void validateSerialNumberUnique(String serialNumber, Long excludeId) {
        if (StrUtil.isBlank(serialNumber)) {
            return;
        }
        IotDeviceDO existDevice = deviceMapper.selectBySerialNumber(serialNumber);
        if (existDevice != null && ObjUtil.notEqual(existDevice.getId(), excludeId)) {
            throw exception(DEVICE_SERIAL_NUMBER_EXISTS);
        }
    }

    private void initDevice(IotDeviceDO device, IotProductDO product) {
        device.setProductId(product.getId()).setProductKey(product.getProductKey())
                .setDeviceType(product.getDeviceType());
        // 生成密钥
        device.setDeviceSecret(generateDeviceSecret());
        // 设置设备状态为未激活
        device.setState(IotDeviceStateEnum.INACTIVE.getState());
    }

    @Override
    public void updateDevice(IotDeviceSaveReqVO updateReqVO) {
        updateReqVO.setDeviceName(null).setProductId(null); // 不允许更新
        // 1.1 校验存在
        IotDeviceDO device = validateDeviceExists(updateReqVO.getId());
        // 1.2 校验父设备是否为合法网关
        if (IotProductDeviceTypeEnum.isGatewaySub(device.getDeviceType())
                && updateReqVO.getGatewayId() != null) {
            validateGatewayDeviceExists(updateReqVO.getGatewayId());
        }
        // 1.3 校验分组存在
        deviceGroupService.validateDeviceGroupExists(updateReqVO.getGroupIds());
        // 1.4 校验设备序列号全局唯一
        validateSerialNumberUnique(updateReqVO.getSerialNumber(), updateReqVO.getId());

        // 2. 更新到数据库
        IotDeviceDO updateObj = BeanUtils.toBean(updateReqVO, IotDeviceDO.class);
        deviceMapper.updateById(updateObj);

        // 3. 清空对应缓存
        deleteDeviceCache(device);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeviceGroup(IotDeviceUpdateGroupReqVO updateReqVO) {
        // 1.1 校验设备存在
        List<IotDeviceDO> devices = deviceMapper.selectByIds(updateReqVO.getIds());
        if (CollUtil.isEmpty(devices)) {
            return;
        }
        // 1.2 校验分组存在
        deviceGroupService.validateDeviceGroupExists(updateReqVO.getGroupIds());

        // 3. 更新设备分组
        deviceMapper.updateBatch(convertList(devices, device -> new IotDeviceDO()
                .setId(device.getId()).setGroupIds(updateReqVO.getGroupIds())));

        // 4. 清空对应缓存
        deleteDeviceCache(devices);
    }

    @Override
    public void deleteDevice(Long id) {
        // 1.1 校验存在
        IotDeviceDO device = validateDeviceExists(id);
        // 1.2 如果是网关设备，检查是否有子设备
        if (device.getGatewayId() != null && deviceMapper.selectCountByGatewayId(id) > 0) {
            throw exception(DEVICE_HAS_CHILDREN);
        }

        // 2. 删除设备
        deviceMapper.deleteById(id);

        // 3. 清空对应缓存
        deleteDeviceCache(device);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDeviceList(Collection<Long> ids) {
        // 1.1 校验存在
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        List<IotDeviceDO> devices = deviceMapper.selectByIds(ids);
        if (CollUtil.isEmpty(devices)) {
            return;
        }
        // 1.2 校验网关设备是否存在
        for (IotDeviceDO device : devices) {
            if (device.getGatewayId() != null && deviceMapper.selectCountByGatewayId(device.getId()) > 0) {
                throw exception(DEVICE_HAS_CHILDREN);
            }
        }

        // 2. 删除设备
        deviceMapper.deleteByIds(ids);

        // 3. 清空对应缓存
        deleteDeviceCache(devices);
    }

    @Override
    public IotDeviceDO validateDeviceExists(Long id) {
        IotDeviceDO device = deviceMapper.selectById(id);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        return device;
    }

    @Override
    public IotDeviceDO validateDeviceExistsFromCache(Long id) {
        IotDeviceDO device = getSelf().getDeviceFromCache(id);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        return device;
    }

    /**
     * 校验网关设备是否存在
     *
     * @param id 设备 ID
     */
    private void validateGatewayDeviceExists(Long id) {
        IotDeviceDO device = deviceMapper.selectById(id);
        if (device == null) {
            throw exception(DEVICE_GATEWAY_NOT_EXISTS);
        }
        if (!IotProductDeviceTypeEnum.isGateway(device.getDeviceType())) {
            throw exception(DEVICE_NOT_GATEWAY);
        }
    }

    @Override
    public IotDeviceDO getDevice(Long id) {
        return deviceMapper.selectById(id);
    }

    @Override
    @Cacheable(value = RedisKeyConstants.DEVICE, key = "#id", unless = "#result == null")
    @TenantIgnore // 忽略租户信息
    public IotDeviceDO getDeviceFromCache(Long id) {
        return deviceMapper.selectById(id);
    }

    @Override
    @Cacheable(value = RedisKeyConstants.DEVICE, key = "#productKey + '_' + #deviceName", unless = "#result == null")
    @TenantIgnore // 忽略租户信息，跨租户 productKey + deviceName 是唯一的
    public IotDeviceDO getDeviceFromCache(String productKey, String deviceName) {
        return deviceMapper.selectByProductKeyAndDeviceName(productKey, deviceName);
    }

    @Override
    public PageResult<IotDeviceDO> getDevicePage(IotDevicePageReqVO pageReqVO) {
        return deviceMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotDeviceDO> getDeviceListByCondition(@Nullable Integer deviceType, @Nullable Long productId) {
        return deviceMapper.selectListByCondition(deviceType, productId);
    }

    @Override
    public List<IotDeviceDO> getDeviceListByState(Integer state) {
        return deviceMapper.selectListByState(state);
    }

    @Override
    public List<IotDeviceDO> getDeviceListByProductId(Long productId) {
        return deviceMapper.selectListByProductId(productId);
    }

    @Override
    public void updateDeviceState(IotDeviceDO device, Integer state) {
        // 1. 更新状态和时间
        IotDeviceDO updateObj = new IotDeviceDO().setId(device.getId()).setState(state);
        if (device.getOnlineTime() == null
                && Objects.equals(state, IotDeviceStateEnum.ONLINE.getState())) {
            updateObj.setActiveTime(LocalDateTime.now());
        }
        if (Objects.equals(state, IotDeviceStateEnum.ONLINE.getState())) {
            updateObj.setOnlineTime(LocalDateTime.now());
        } else if (Objects.equals(state, IotDeviceStateEnum.OFFLINE.getState())) {
            updateObj.setOfflineTime(LocalDateTime.now());
        }
        deviceMapper.updateById(updateObj);

        // 2. 清空对应缓存
        deleteDeviceCache(device);
    }

    @Override
    public void updateDeviceState(Long id, Integer state) {
        // 校验存在
        IotDeviceDO device = validateDeviceExists(id);
        // 执行更新
        updateDeviceState(device, state);
    }

    @Override
    public Long getDeviceCountByProductId(Long productId) {
        return deviceMapper.selectCountByProductId(productId);
    }

    @Override
    public Long getDeviceCountByGroupId(Long groupId) {
        return deviceMapper.selectCountByGroupId(groupId);
    }

    /**
     * 生成 deviceSecret
     *
     * @return 生成的 deviceSecret
     */
    private String generateDeviceSecret() {
        return IdUtil.fastSimpleUUID();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public IotDeviceImportRespVO importDevice(List<IotDeviceImportExcelVO> importDevices, boolean updateSupport) {
        // 1. 参数校验
        if (CollUtil.isEmpty(importDevices)) {
            throw exception(DEVICE_IMPORT_LIST_IS_EMPTY);
        }

        // 2. 遍历，逐个创建 or 更新
        IotDeviceImportRespVO respVO = IotDeviceImportRespVO.builder().createDeviceNames(new ArrayList<>())
                .updateDeviceNames(new ArrayList<>()).failureDeviceNames(new LinkedHashMap<>()).build();
        importDevices.forEach(importDevice -> {
            try {
                // 2.1.1 校验字段是否符合要求
                try {
                    ValidationUtils.validate(importDevice);
                } catch (ConstraintViolationException ex) {
                    respVO.getFailureDeviceNames().put(importDevice.getDeviceName(), ex.getMessage());
                    return;
                }
                // 2.1.2 校验产品是否存在
                IotProductDO product = productService.validateProductExists(importDevice.getProductKey());
                // 2.1.3 校验父设备是否存在
                Long gatewayId = null;
                if (StrUtil.isNotEmpty(importDevice.getParentDeviceName())) {
                    IotDeviceDO gatewayDevice = deviceMapper.selectByDeviceName(importDevice.getParentDeviceName());
                    if (gatewayDevice == null) {
                        throw exception(DEVICE_GATEWAY_NOT_EXISTS);
                    }
                    if (!IotProductDeviceTypeEnum.isGateway(gatewayDevice.getDeviceType())) {
                        throw exception(DEVICE_NOT_GATEWAY);
                    }
                    gatewayId = gatewayDevice.getId();
                }
                // 2.1.4 校验设备分组是否存在
                Set<Long> groupIds = new HashSet<>();
                if (StrUtil.isNotEmpty(importDevice.getGroupNames())) {
                    String[] groupNames = importDevice.getGroupNames().split(",");
                    for (String groupName : groupNames) {
                        IotDeviceGroupDO group = deviceGroupService.getDeviceGroupByName(groupName);
                        if (group == null) {
                            throw exception(DEVICE_GROUP_NOT_EXISTS);
                        }
                        groupIds.add(group.getId());
                    }
                }

                // 2.2.1 判断如果不存在，在进行插入
                IotDeviceDO existDevice = deviceMapper.selectByDeviceName(importDevice.getDeviceName());
                if (existDevice == null) {
                    createDevice(new IotDeviceSaveReqVO()
                            .setDeviceName(importDevice.getDeviceName())
                            .setProductId(product.getId()).setGatewayId(gatewayId).setGroupIds(groupIds)
                            .setLocationType(importDevice.getLocationType()));
                    respVO.getCreateDeviceNames().add(importDevice.getDeviceName());
                    return;
                }
                // 2.2.2 如果存在，判断是否允许更新
                if (!updateSupport) {
                    throw exception(DEVICE_KEY_EXISTS);
                }
                updateDevice(new IotDeviceSaveReqVO().setId(existDevice.getId())
                        .setGatewayId(gatewayId).setGroupIds(groupIds).setLocationType(importDevice.getLocationType()));
                respVO.getUpdateDeviceNames().add(importDevice.getDeviceName());
            } catch (ServiceException ex) {
                respVO.getFailureDeviceNames().put(importDevice.getDeviceName(), ex.getMessage());
            }
        });
        return respVO;
    }

    @Override
    public IotDeviceAuthInfoRespVO getDeviceAuthInfo(Long id) {
        IotDeviceDO device = validateDeviceExists(id);
        // 使用 IotDeviceAuthUtils 生成认证信息
        IotDeviceAuthUtils.AuthInfo authInfo = IotDeviceAuthUtils.getAuthInfo(
                device.getProductKey(), device.getDeviceName(), device.getDeviceSecret());
        return BeanUtils.toBean(authInfo, IotDeviceAuthInfoRespVO.class);
    }

    private void deleteDeviceCache(IotDeviceDO device) {
        // 保证 Spring AOP 触发
        getSelf().deleteDeviceCache0(device);
    }

    private void deleteDeviceCache(List<IotDeviceDO> devices) {
        devices.forEach(this::deleteDeviceCache);
    }

    @SuppressWarnings("unused")
    @Caching(evict = {
        @CacheEvict(value = RedisKeyConstants.DEVICE, key = "#device.id"),
        @CacheEvict(value = RedisKeyConstants.DEVICE, key = "#device.productKey + '_' + #device.deviceName")
    })
    public void deleteDeviceCache0(IotDeviceDO device) {
    }

    @Override
    public Long getDeviceCount(LocalDateTime createTime) {
        return deviceMapper.selectCountByCreateTime(createTime);
    }

    @Override
    public Map<Long, Integer> getDeviceCountMapByProductId() {
        return deviceMapper.selectDeviceCountMapByProductId();
    }

    @Override
    public Map<Integer, Long> getDeviceCountMapByState() {
        return deviceMapper.selectDeviceCountGroupByState();
    }

    @Override
    public List<IotDeviceDO> getDeviceListByProductKeyAndNames(String productKey, List<String> deviceNames) {
        if (StrUtil.isBlank(productKey) || CollUtil.isEmpty(deviceNames)) {
            return Collections.emptyList();
        }
        return deviceMapper.selectByProductKeyAndDeviceNames(productKey, deviceNames);
    }

    @Override
    public boolean authDevice(IotDeviceAuthReqDTO authReqDTO) {
        // 1. 校验设备是否存在
        IotDeviceAuthUtils.DeviceInfo deviceInfo = IotDeviceAuthUtils.parseUsername(authReqDTO.getUsername());
        if (deviceInfo == null) {
            log.error("[authDevice][认证失败，username({}) 格式不正确]", authReqDTO.getUsername());
            return false;
        }
        String deviceName = deviceInfo.getDeviceName();
        String productKey = deviceInfo.getProductKey();
        IotDeviceDO device = getSelf().getDeviceFromCache(productKey, deviceName);
        if (device == null) {
            log.warn("[authDevice][设备({}/{}) 不存在]", productKey, deviceName);
            return false;
        }

        // 2. 校验密码
        IotDeviceAuthUtils.AuthInfo authInfo = IotDeviceAuthUtils.getAuthInfo(productKey, deviceName, device.getDeviceSecret());
        if (ObjUtil.notEqual(authInfo.getPassword(), authReqDTO.getPassword())) {
            log.error("[authDevice][设备({}/{}) 密码不正确]", productKey, deviceName);
            return false;
        }
        return true;
    }

    @Override
    public List<IotDeviceDO> validateDeviceListExists(Collection<Long> ids) {
        List<IotDeviceDO> devices = getDeviceList(ids);
        if (devices.size() != ids.size()) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        return devices;
    }

    @Override
    public List<IotDeviceDO> getDeviceList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return deviceMapper.selectByIds(ids);
    }

    @Override
    public void updateDeviceFirmware(Long deviceId, Long firmwareId) {
        // 1. 校验设备是否存在
        IotDeviceDO device = validateDeviceExists(deviceId);
        
        // 2. 更新设备固件版本
        IotDeviceDO updateObj = new IotDeviceDO().setId(deviceId).setFirmwareId(firmwareId);
        deviceMapper.updateById(updateObj);
        
        // 3. 清空对应缓存
        deleteDeviceCache(device);
    }

    private IotDeviceServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
