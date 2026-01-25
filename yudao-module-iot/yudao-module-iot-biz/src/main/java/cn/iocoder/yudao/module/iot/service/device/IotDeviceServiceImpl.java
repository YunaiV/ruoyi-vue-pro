package cn.iocoder.yudao.module.iot.service.device;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.*;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotSubDeviceRegisterFullReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterRespDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotSubDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotSubDeviceRegisterRespDTO;
import cn.iocoder.yudao.module.iot.core.topic.topo.IotDeviceTopoAddReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.topo.IotDeviceTopoChangeReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.topo.IotDeviceTopoDeleteReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.topo.IotDeviceTopoGetRespDTO;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceGroupDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants;
import cn.iocoder.yudao.module.iot.enums.product.IotProductDeviceTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.message.IotDeviceMessageService;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;
import static java.util.Collections.singletonList;

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
    @Resource
    @Lazy // 延迟加载，解决循环依赖
    private IotDeviceMessageService deviceMessageService;

    private IotDeviceServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

    @Override
    public Long createDevice(IotDeviceSaveReqVO createReqVO) {
        return createDevice0(createReqVO).getId();
    }

    private IotDeviceDO createDevice0(IotDeviceSaveReqVO createReqVO) {
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
        return device;
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
                .setDeviceType(product.getDeviceType())
                .setDeviceSecret(generateDeviceSecret()) // 生成密钥
                .setState(IotDeviceStateEnum.INACTIVE.getState()); // 默认未激活
    }

    private String generateDeviceSecret() {
        return IdUtil.fastSimpleUUID();
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
        // 1.2 如果是网关设备，检查是否有子设备绑定
        if (IotProductDeviceTypeEnum.isGateway(device.getDeviceType())
                && deviceMapper.selectCountByGatewayId(id) > 0) {
            throw exception(DEVICE_GATEWAY_HAS_SUB);
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
        // 1.2 如果是网关设备，检查是否有子设备绑定
        for (IotDeviceDO device : devices) {
            if (IotProductDeviceTypeEnum.isGateway(device.getDeviceType())
                    && deviceMapper.selectCountByGatewayId(device.getId()) > 0) {
                throw exception(DEVICE_GATEWAY_HAS_SUB);
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

        // 3. 网关设备下线时，联动所有子设备下线
        if (Objects.equals(state, IotDeviceStateEnum.OFFLINE.getState())
                && IotProductDeviceTypeEnum.isGateway(device.getDeviceType())) {
            handleGatewayOffline(device);
        }
    }

    /**
     * 处理网关下线，联动所有子设备下线
     *
     * @param gatewayDevice 网关设备
     */
    private void handleGatewayOffline(IotDeviceDO gatewayDevice) {
        List<IotDeviceDO> subDevices = deviceMapper.selectListByGatewayId(gatewayDevice.getId());
        if (CollUtil.isEmpty(subDevices)) {
            return;
        }
        for (IotDeviceDO subDevice : subDevices) {
            if (Objects.equals(subDevice.getState(), IotDeviceStateEnum.ONLINE.getState())) {
                try {
                    updateDeviceState(subDevice, IotDeviceStateEnum.OFFLINE.getState());
                    log.info("[handleGatewayOffline][网关({}/{}) 下线，子设备({}/{}) 联动下线]",
                            gatewayDevice.getProductKey(), gatewayDevice.getDeviceName(),
                            subDevice.getProductKey(), subDevice.getDeviceName());
                } catch (Exception ex) {
                    log.error("[handleGatewayOffline][子设备({}/{}) 下线失败]",
                            subDevice.getProductKey(), subDevice.getDeviceName(), ex);
                }
            }
        }
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
                            .setProductId(product.getId()).setGatewayId(gatewayId).setGroupIds(groupIds));
                    respVO.getCreateDeviceNames().add(importDevice.getDeviceName());
                    return;
                }
                // 2.2.2 如果存在，判断是否允许更新
                if (!updateSupport) {
                    throw exception(DEVICE_KEY_EXISTS);
                }
                updateDevice(new IotDeviceSaveReqVO().setId(existDevice.getId())
                        .setGatewayId(gatewayId).setGroupIds(groupIds));
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
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(
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
        IotDeviceIdentity deviceInfo = IotDeviceAuthUtils.parseUsername(authReqDTO.getUsername());
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
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(productKey, deviceName, device.getDeviceSecret());
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

    @Override
    public void updateDeviceLocation(IotDeviceDO device, BigDecimal longitude, BigDecimal latitude) {
        // 1. 更新定位信息
        deviceMapper.updateById(new IotDeviceDO().setId(device.getId())
                .setLongitude(longitude).setLatitude(latitude));

        // 2. 清空对应缓存
        deleteDeviceCache(device);
    }

    @Override
    public List<IotDeviceDO> getDeviceListByHasLocation() {
        return deviceMapper.selectListByHasLocation();
    }

    // ========== 网关-拓扑管理（后台操作） ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindDeviceGateway(Collection<Long> subIds, Long gatewayId) {
        if (CollUtil.isEmpty(subIds)) {
            return;
        }
        // 1.1 校验网关设备存在且类型正确
        validateGatewayDeviceExists(gatewayId);
        // 1.2 校验每个设备是否可绑定
        List<IotDeviceDO> devices = deviceMapper.selectByIds(subIds);
        for (IotDeviceDO device : devices) {
            checkSubDeviceCanBind(device, gatewayId);
        }

        // 2. 批量更新数据库
        List<IotDeviceDO> updateList = convertList(devices, device ->
                new IotDeviceDO().setId(device.getId()).setGatewayId(gatewayId));
        deviceMapper.updateBatch(updateList);

        // 3. 清空对应缓存
        deleteDeviceCache(devices);

        // 4. 下发网关设备拓扑变更通知（增加）
        sendTopoChangeNotify(gatewayId, IotDeviceTopoChangeReqDTO.STATUS_CREATE, devices);
    }

    private void checkSubDeviceCanBind(IotDeviceDO device, Long gatewayId) {
        if (!IotProductDeviceTypeEnum.isGatewaySub(device.getDeviceType())) {
            throw exception(DEVICE_NOT_GATEWAY_SUB, device.getProductKey(), device.getDeviceName());
        }
        // 已绑定到其他网关，拒绝绑定（需先解绑）
        if (device.getGatewayId() != null && ObjUtil.notEqual(device.getGatewayId(), gatewayId)) {
            throw exception(DEVICE_GATEWAY_BINDTO_EXISTS, device.getProductKey(), device.getDeviceName());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindDeviceGateway(Collection<Long> subIds, Long gatewayId) {
        // 1. 校验设备存在
        if (CollUtil.isEmpty(subIds)) {
            return;
        }
        List<IotDeviceDO> devices = deviceMapper.selectByIds(subIds);
        devices.removeIf(device -> ObjUtil.notEqual(device.getGatewayId(), gatewayId));
        if (CollUtil.isEmpty(devices)) {
            return;
        }

        // 2. 批量更新数据库（将 gatewayId 设置为 null）
        deviceMapper.updateGatewayIdBatch(convertList(devices, IotDeviceDO::getId), null);

        // 3. 清空对应缓存
        deleteDeviceCache(devices);

        // 4. 下发网关设备拓扑变更通知（删除）
        sendTopoChangeNotify(gatewayId, IotDeviceTopoChangeReqDTO.STATUS_DELETE, devices);
    }

    @Override
    public PageResult<IotDeviceDO> getUnboundSubDevicePage(IotDevicePageReqVO pageReqVO) {
        return deviceMapper.selectUnboundSubDevicePage(pageReqVO);
    }

    @Override
    public List<IotDeviceDO> getDeviceListByGatewayId(Long gatewayId) {
        return deviceMapper.selectListByGatewayId(gatewayId);
    }

    // ========== 网关-拓扑管理（设备上报） ==========

    @Override
    public List<IotDeviceIdentity> handleTopoAddMessage(IotDeviceMessage message, IotDeviceDO gatewayDevice) {
        // 1.1 校验网关设备类型
        if (!IotProductDeviceTypeEnum.isGateway(gatewayDevice.getDeviceType())) {
            throw exception(DEVICE_NOT_GATEWAY);
        }
        // 1.2 解析参数
        IotDeviceTopoAddReqDTO params = JsonUtils.convertObject(message.getParams(), IotDeviceTopoAddReqDTO.class);
        if (params == null || CollUtil.isEmpty(params.getSubDevices())) {
            throw exception(DEVICE_TOPO_PARAMS_INVALID);
        }

        // 2. 遍历处理每个子设备
        List<IotDeviceIdentity> addedSubDevices = new ArrayList<>();
        for (IotDeviceAuthReqDTO subDeviceAuth : params.getSubDevices()) {
            try {
                IotDeviceDO subDevice = addDeviceTopo(gatewayDevice, subDeviceAuth);
                addedSubDevices.add(new IotDeviceIdentity(subDevice.getProductKey(), subDevice.getDeviceName()));
            } catch (Exception ex) {
                log.warn("[handleTopoAddMessage][网关({}/{}) 添加子设备失败，message={}]",
                        gatewayDevice.getProductKey(), gatewayDevice.getDeviceName(), message, ex);
            }
        }

        // 3. 返回响应数据（包含成功添加的子设备列表）
        return addedSubDevices;
    }

    private IotDeviceDO addDeviceTopo(IotDeviceDO gatewayDevice, IotDeviceAuthReqDTO subDeviceAuth) {
        // 1.1 解析子设备信息
        IotDeviceIdentity subDeviceInfo = IotDeviceAuthUtils.parseUsername(subDeviceAuth.getUsername());
        if (subDeviceInfo == null) {
            throw exception(DEVICE_TOPO_SUB_DEVICE_USERNAME_INVALID);
        }
        // 1.2 校验子设备认证信息
        if (!authDevice(subDeviceAuth)) {
            throw exception(DEVICE_TOPO_SUB_DEVICE_AUTH_FAILED);
        }
        // 1.3 获取子设备
        IotDeviceDO subDevice = getSelf().getDeviceFromCache(subDeviceInfo.getProductKey(), subDeviceInfo.getDeviceName());
        if (subDevice == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        // 1.4 校验子设备类型
        checkSubDeviceCanBind(subDevice, gatewayDevice.getId());

        // 2. 更新数据库
        deviceMapper.updateById(new IotDeviceDO().setId(subDevice.getId()).setGatewayId(gatewayDevice.getId()));
        log.info("[addDeviceTopo][网关({}/{}) 绑定子设备({}/{})]",
                gatewayDevice.getProductKey(), gatewayDevice.getDeviceName(),
                subDevice.getProductKey(), subDevice.getDeviceName());

        // 3. 清空对应缓存
        deleteDeviceCache(subDevice);
        return subDevice;
    }

    @Override
    public List<IotDeviceIdentity> handleTopoDeleteMessage(IotDeviceMessage message, IotDeviceDO gatewayDevice) {
        // 1.1 校验网关设备类型
        if (!IotProductDeviceTypeEnum.isGateway(gatewayDevice.getDeviceType())) {
            throw exception(DEVICE_NOT_GATEWAY);
        }
        // 1.2 解析参数
        IotDeviceTopoDeleteReqDTO params = JsonUtils.convertObject(message.getParams(), IotDeviceTopoDeleteReqDTO.class);
        if (params == null || CollUtil.isEmpty(params.getSubDevices())) {
            throw exception(DEVICE_TOPO_PARAMS_INVALID);
        }

        // 2. 遍历处理每个子设备
        List<IotDeviceIdentity> deletedSubDevices = new ArrayList<>();
        for (IotDeviceIdentity subDeviceIdentity : params.getSubDevices()) {
            try {
                deleteDeviceTopo(gatewayDevice, subDeviceIdentity);
                deletedSubDevices.add(subDeviceIdentity);
            } catch (Exception ex) {
                log.warn("[handleTopoDeleteMessage][网关({}/{}) 删除子设备失败，productKey={}, deviceName={}]",
                        gatewayDevice.getProductKey(), gatewayDevice.getDeviceName(),
                        subDeviceIdentity.getProductKey(), subDeviceIdentity.getDeviceName(), ex);
            }
        }

        // 3. 返回响应数据（包含成功删除的子设备列表）
        return deletedSubDevices;
    }

    private void deleteDeviceTopo(IotDeviceDO gatewayDevice, IotDeviceIdentity subDeviceIdentity) {
        // 1.1 获取子设备
        IotDeviceDO subDevice = getSelf().getDeviceFromCache(subDeviceIdentity.getProductKey(), subDeviceIdentity.getDeviceName());
        if (subDevice == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        // 1.2 校验子设备是否绑定到该网关
        if (ObjUtil.notEqual(subDevice.getGatewayId(), gatewayDevice.getId())) {
            throw exception(DEVICE_TOPO_SUB_NOT_BINDTO_GATEWAY,
                    subDeviceIdentity.getProductKey(), subDeviceIdentity.getDeviceName());
        }

        // 2. 更新数据库（将 gatewayId 设置为 null）
        deviceMapper.updateGatewayIdBatch(singletonList(subDevice.getId()), null);
        log.info("[deleteDeviceTopo][网关({}/{}) 解绑子设备({}/{})]",
                gatewayDevice.getProductKey(), gatewayDevice.getDeviceName(),
                subDevice.getProductKey(), subDevice.getDeviceName());

        // 3. 清空对应缓存
        deleteDeviceCache(subDevice);

        // 4. 子设备下线
        if (Objects.equals(subDevice.getState(), IotDeviceStateEnum.ONLINE.getState())) {
            updateDeviceState(subDevice, IotDeviceStateEnum.OFFLINE.getState());
        }
    }

    @Override
    public IotDeviceTopoGetRespDTO handleTopoGetMessage(IotDeviceDO gatewayDevice) {
        // 1. 校验网关设备类型
        if (!IotProductDeviceTypeEnum.isGateway(gatewayDevice.getDeviceType())) {
            throw exception(DEVICE_NOT_GATEWAY);
        }

        // 2. 获取子设备列表并转换
        List<IotDeviceDO> subDevices = deviceMapper.selectListByGatewayId(gatewayDevice.getId());
        List<IotDeviceIdentity> subDeviceIdentities = convertList(subDevices, subDevice ->
                new IotDeviceIdentity(subDevice.getProductKey(), subDevice.getDeviceName()));
        return new IotDeviceTopoGetRespDTO().setSubDevices(subDeviceIdentities);
    }

    /**
     * 发送拓扑变更通知给网关设备
     *
     * @param gatewayId  网关设备编号
     * @param status     变更状态（0-创建, 1-删除）
     * @param subDevices 子设备列表
     * @see <a href="https://help.aliyun.com/zh/marketplace/notify-gateway-topology-changes">阿里云 - 通知网关拓扑关系变化</a>
     */
    private void sendTopoChangeNotify(Long gatewayId, Integer status, List<IotDeviceDO> subDevices) {
        if (CollUtil.isEmpty(subDevices)) {
            return;
        }
        // 1. 获取网关设备
        IotDeviceDO gatewayDevice = deviceMapper.selectById(gatewayId);
        if (gatewayDevice == null) {
            log.warn("[sendTopoChangeNotify][网关设备({}) 不存在，无法发送拓扑变更通知]", gatewayId);
            return;
        }

        try {
            // 2.1 构建拓扑变更通知消息
            List<IotDeviceIdentity> subList = convertList(subDevices, subDevice ->
                    new IotDeviceIdentity(subDevice.getProductKey(), subDevice.getDeviceName()));
            IotDeviceTopoChangeReqDTO params = new IotDeviceTopoChangeReqDTO(status, subList);
            IotDeviceMessage notifyMessage = IotDeviceMessage.requestOf(
                    IotDeviceMessageMethodEnum.TOPO_CHANGE.getMethod(), params);

            // 2.2 发送消息
            deviceMessageService.sendDeviceMessage(notifyMessage, gatewayDevice);
            log.info("[sendTopoChangeNotify][网关({}/{}) 发送拓扑变更通知成功，status={}, subDevices={}]",
                    gatewayDevice.getProductKey(), gatewayDevice.getDeviceName(),
                    status, subList);
        } catch (Exception ex) {
            log.error("[sendTopoChangeNotify][网关({}/{}) 发送拓扑变更通知失败，status={}]",
                    gatewayDevice.getProductKey(), gatewayDevice.getDeviceName(), status, ex);
        }
    }

    // ========== 设备动态注册 ==========

    @Override
    public IotDeviceRegisterRespDTO registerDevice(IotDeviceRegisterReqDTO reqDTO) {
        // 1.1 校验产品
        IotProductDO product = TenantUtils.executeIgnore(() ->
                productService.getProductByProductKey(reqDTO.getProductKey()));
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        // 1.2 校验产品是否开启动态注册
        if (BooleanUtil.isFalse(product.getRegisterEnabled())) {
            throw exception(DEVICE_REGISTER_DISABLED);
        }
        // 1.3 验证 productSecret
        if (ObjUtil.notEqual(product.getProductSecret(), reqDTO.getProductSecret())) {
            throw exception(DEVICE_REGISTER_SECRET_INVALID);
        }
        return TenantUtils.execute(product.getTenantId(), () -> {
            // 1.4 校验设备是否已存在（已存在则不允许重复注册）
            IotDeviceDO device = getSelf().getDeviceFromCache(reqDTO.getProductKey(), reqDTO.getDeviceName());
            if (device != null) {
                throw exception(DEVICE_REGISTER_ALREADY_EXISTS);
            }

            // 2.1 自动创建设备
            IotDeviceSaveReqVO createReqVO = new IotDeviceSaveReqVO()
                    .setDeviceName(reqDTO.getDeviceName())
                    .setProductId(product.getId());
            device = createDevice0(createReqVO);
            log.info("[registerDevice][产品({}) 自动创建设备({})]",
                    reqDTO.getProductKey(), reqDTO.getDeviceName());
            // 2.2 返回设备密钥
            return new IotDeviceRegisterRespDTO(device.getProductKey(), device.getDeviceName(), device.getDeviceSecret());
        });
    }

    @Override
    public List<IotSubDeviceRegisterRespDTO> registerSubDevices(IotSubDeviceRegisterFullReqDTO reqDTO) {
        // 1. 校验网关设备
        IotDeviceDO gatewayDevice = getSelf().getDeviceFromCache(reqDTO.getGatewayProductKey(), reqDTO.getGatewayDeviceName());

        // 2. 遍历注册每个子设备
        return TenantUtils.execute(gatewayDevice.getTenantId(), () ->
                registerSubDevices0(gatewayDevice, reqDTO.getSubDevices()));
    }

    @Override
    public List<IotSubDeviceRegisterRespDTO> handleSubDeviceRegisterMessage(IotDeviceMessage message, IotDeviceDO gatewayDevice) {
        // 1. 解析参数
        if (!(message.getParams() instanceof List)) {
            throw exception(DEVICE_SUB_REGISTER_PARAMS_INVALID);
        }
        List<IotSubDeviceRegisterReqDTO> subDevices = JsonUtils.convertList(message.getParams(), IotSubDeviceRegisterReqDTO.class);

        // 2. 遍历注册每个子设备
        return registerSubDevices0(gatewayDevice, subDevices);
    }

    private List<IotSubDeviceRegisterRespDTO> registerSubDevices0(IotDeviceDO gatewayDevice,
                                                                   List<IotSubDeviceRegisterReqDTO> subDevices) {
        // 1.1 校验网关设备
        if (gatewayDevice == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        if (!IotProductDeviceTypeEnum.isGateway(gatewayDevice.getDeviceType())) {
            throw exception(DEVICE_NOT_GATEWAY);
        }
        // 1.2 注册设备不能为空
        if (CollUtil.isEmpty(subDevices)) {
            throw exception(DEVICE_SUB_REGISTER_PARAMS_INVALID);
        }

        // 2. 遍历注册每个子设备
        List<IotSubDeviceRegisterRespDTO> results = new ArrayList<>(subDevices.size());
        for (IotSubDeviceRegisterReqDTO subDevice : subDevices) {
            try {
                IotDeviceDO device = registerSubDevice0(gatewayDevice, subDevice);
                results.add(new IotSubDeviceRegisterRespDTO(
                        subDevice.getProductKey(), subDevice.getDeviceName(), device.getDeviceSecret()));
            } catch (Exception ex) {
                log.error("[registerSubDevices0][子设备({}/{}) 注册失败]",
                        subDevice.getProductKey(), subDevice.getDeviceName(), ex);
            }
        }
        return results;
    }

    private IotDeviceDO registerSubDevice0(IotDeviceDO gatewayDevice, IotSubDeviceRegisterReqDTO params) {
        // 1.1 校验产品
        IotProductDO product = productService.getProductByProductKey(params.getProductKey());
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        // 1.2 校验产品是否为网关子设备类型
        if (!IotProductDeviceTypeEnum.isGatewaySub(product.getDeviceType())) {
            throw exception(DEVICE_SUB_REGISTER_PRODUCT_NOT_GATEWAY_SUB, params.getProductKey());
        }
        // 1.3 校验设备是否已存在（子设备动态注册：设备必须已预注册）
        IotDeviceDO existDevice = getSelf().getDeviceFromCache(params.getProductKey(), params.getDeviceName());
        if (existDevice == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        // 1.4 校验是否绑定到其他网关
        if (existDevice.getGatewayId() != null && ObjUtil.notEqual(existDevice.getGatewayId(), gatewayDevice.getId())) {
            throw exception(DEVICE_GATEWAY_BINDTO_EXISTS,
                    existDevice.getProductKey(), existDevice.getDeviceName());
        }

        // 2. 绑定到网关（如果尚未绑定）
        if (existDevice.getGatewayId() == null) {
            // 2.1 更新数据库
            deviceMapper.updateById(new IotDeviceDO().setId(existDevice.getId()).setGatewayId(gatewayDevice.getId()));
            // 2.2 清空对应缓存
            deleteDeviceCache(existDevice);
            log.info("[registerSubDevice][网关({}/{}) 绑定子设备({}/{})]",
                    gatewayDevice.getProductKey(), gatewayDevice.getDeviceName(),
                    existDevice.getProductKey(), existDevice.getDeviceName());
        }
        return existDevice;
    }

}
