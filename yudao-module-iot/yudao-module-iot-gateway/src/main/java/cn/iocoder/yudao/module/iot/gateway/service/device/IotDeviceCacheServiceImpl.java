package cn.iocoder.yudao.module.iot.gateway.service.device;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceInfoReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceInfoRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * IoT 设备缓存 Service 实现类
 * <p>
 * 使用本地缓存 + 远程 API 的方式获取设备信息，提高性能并避免敏感信息传输
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class IotDeviceCacheServiceImpl implements IotDeviceCacheService {

    /**
     * 设备信息本地缓存
     * Key: deviceId
     * Value: DeviceInfo
     */
    private final ConcurrentHashMap<Long, DeviceInfo> deviceIdCache = new ConcurrentHashMap<>();

    /**
     * 设备名称到设备ID的映射缓存
     * Key: productKey:deviceName
     * Value: deviceId
     */
    private final ConcurrentHashMap<String, Long> deviceNameCache = new ConcurrentHashMap<>();

    /**
     * 锁对象，防止并发请求同一设备信息
     */
    private final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    @Override
    public DeviceInfo getDeviceInfo(String productKey, String deviceName) {
        if (StrUtil.isEmpty(productKey) || StrUtil.isEmpty(deviceName)) {
            log.warn("[getDeviceInfo][参数为空][productKey: {}][deviceName: {}]", productKey, deviceName);
            return null;
        }

        String cacheKey = buildDeviceNameCacheKey(productKey, deviceName);

        // 1. 先从缓存获取 deviceId
        Long deviceId = deviceNameCache.get(cacheKey);
        if (deviceId != null) {
            DeviceInfo deviceInfo = deviceIdCache.get(deviceId);
            if (deviceInfo != null) {
                log.debug("[getDeviceInfo][缓存命中][productKey: {}][deviceName: {}][deviceId: {}]",
                        productKey, deviceName, deviceId);
                return deviceInfo;
            }
        }

        // 2. 缓存未命中，从远程 API 获取
        return loadDeviceInfoFromApi(productKey, deviceName, cacheKey);
    }

    @Override
    public DeviceInfo getDeviceInfoById(Long deviceId) {
        if (deviceId == null) {
            log.warn("[getDeviceInfoById][deviceId 为空]");
            return null;
        }

        // 1. 先从缓存获取
        DeviceInfo deviceInfo = deviceIdCache.get(deviceId);
        if (deviceInfo != null) {
            log.debug("[getDeviceInfoById][缓存命中][deviceId: {}]", deviceId);
            return deviceInfo;
        }

        // 2. 缓存未命中，从远程 API 获取
        return loadDeviceInfoByIdFromApi(deviceId);
    }

    @Override
    public void evictDeviceCache(Long deviceId) {
        if (deviceId == null) {
            return;
        }

        DeviceInfo deviceInfo = deviceIdCache.remove(deviceId);
        if (deviceInfo != null) {
            String cacheKey = buildDeviceNameCacheKey(deviceInfo.getProductKey(), deviceInfo.getDeviceName());
            deviceNameCache.remove(cacheKey);
            log.info("[evictDeviceCache][清除设备缓存][deviceId: {}]", deviceId);
        }
    }

    @Override
    public void evictDeviceCache(String productKey, String deviceName) {
        if (StrUtil.isEmpty(productKey) || StrUtil.isEmpty(deviceName)) {
            return;
        }

        String cacheKey = buildDeviceNameCacheKey(productKey, deviceName);
        Long deviceId = deviceNameCache.remove(cacheKey);
        if (deviceId != null) {
            deviceIdCache.remove(deviceId);
            log.info("[evictDeviceCache][清除设备缓存][productKey: {}][deviceName: {}]", productKey, deviceName);
        }
    }

    /**
     * 从远程 API 加载设备信息
     *
     * @param productKey 产品标识
     * @param deviceName 设备名称
     * @param cacheKey   缓存键
     * @return 设备信息
     */
    private DeviceInfo loadDeviceInfoFromApi(String productKey, String deviceName, String cacheKey) {
        // 使用锁防止并发请求同一设备信息
        ReentrantLock lock = lockMap.computeIfAbsent(cacheKey, k -> new ReentrantLock());
        lock.lock();
        try {
            // 双重检查，防止重复请求
            Long deviceId = deviceNameCache.get(cacheKey);
            if (deviceId != null) {
                DeviceInfo deviceInfo = deviceIdCache.get(deviceId);
                if (deviceInfo != null) {
                    return deviceInfo;
                }
            }

            log.info("[loadDeviceInfoFromApi][从远程API获取设备信息][productKey: {}][deviceName: {}]",
                    productKey, deviceName);

            try {
                // 调用远程 API 获取设备信息
                IotDeviceCommonApi deviceCommonApi = SpringUtil.getBean(IotDeviceCommonApi.class);
                IotDeviceInfoReqDTO reqDTO = new IotDeviceInfoReqDTO();
                reqDTO.setProductKey(productKey);
                reqDTO.setDeviceName(deviceName);

                CommonResult<IotDeviceInfoRespDTO> result = deviceCommonApi.getDeviceInfo(reqDTO);

                if (result == null || !result.isSuccess() || result.getData() == null) {
                    log.warn("[loadDeviceInfoFromApi][远程API调用失败][productKey: {}][deviceName: {}][result: {}]",
                            productKey, deviceName, result);
                    return null;
                }

                IotDeviceInfoRespDTO respDTO = result.getData();
                DeviceInfo deviceInfo = new DeviceInfo(
                        respDTO.getDeviceId(),
                        respDTO.getProductKey(),
                        respDTO.getDeviceName(),
                        respDTO.getDeviceKey(),
                        respDTO.getTenantId());

                // 缓存设备信息
                cacheDeviceInfo(deviceInfo, cacheKey);

                log.info("[loadDeviceInfoFromApi][设备信息获取成功并已缓存][deviceInfo: {}]", deviceInfo);
                return deviceInfo;

            } catch (Exception e) {
                log.error("[loadDeviceInfoFromApi][远程API调用异常][productKey: {}][deviceName: {}]",
                        productKey, deviceName, e);
                return null;
            }
        } finally {
            lock.unlock();
            // 清理锁对象，避免内存泄漏
            if (lockMap.size() > 1000) { // 简单的清理策略
                lockMap.entrySet().removeIf(entry -> !entry.getValue().hasQueuedThreads());
            }
        }
    }

    /**
     * 从远程 API 根据 deviceId 加载设备信息
     *
     * @param deviceId 设备编号
     * @return 设备信息
     */
    private DeviceInfo loadDeviceInfoByIdFromApi(Long deviceId) {
        String lockKey = "deviceId:" + deviceId;
        ReentrantLock lock = lockMap.computeIfAbsent(lockKey, k -> new ReentrantLock());
        lock.lock();
        try {
            // 双重检查
            DeviceInfo deviceInfo = deviceIdCache.get(deviceId);
            if (deviceInfo != null) {
                return deviceInfo;
            }

            log.info("[loadDeviceInfoByIdFromApi][从远程API获取设备信息][deviceId: {}]", deviceId);

            try {
                // TODO: 这里需要添加根据 deviceId 获取设备信息的 API
                // 暂时返回 null，等待 API 完善
                log.warn("[loadDeviceInfoByIdFromApi][根据deviceId获取设备信息的API尚未实现][deviceId: {}]", deviceId);
                return null;

            } catch (Exception e) {
                log.error("[loadDeviceInfoByIdFromApi][远程API调用异常][deviceId: {}]", deviceId, e);
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 缓存设备信息
     *
     * @param deviceInfo 设备信息
     * @param cacheKey   缓存键
     */
    private void cacheDeviceInfo(DeviceInfo deviceInfo, String cacheKey) {
        if (deviceInfo != null && deviceInfo.getDeviceId() != null) {
            deviceIdCache.put(deviceInfo.getDeviceId(), deviceInfo);
            deviceNameCache.put(cacheKey, deviceInfo.getDeviceId());
        }
    }

    /**
     * 构建设备名称缓存键
     *
     * @param productKey 产品标识
     * @param deviceName 设备名称
     * @return 缓存键
     */
    private String buildDeviceNameCacheKey(String productKey, String deviceName) {
        return productKey + ":" + deviceName;
    }

}