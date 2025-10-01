package cn.iocoder.yudao.module.iot.gateway.service.device;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceGetReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static cn.iocoder.yudao.framework.common.util.cache.CacheUtils.buildAsyncReloadingCache;

/**
 * IoT 设备信息 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class IotDeviceServiceImpl implements IotDeviceService {

    private static final Duration CACHE_EXPIRE = Duration.ofMinutes(1);

    /**
     * 通过 id 查询设备的缓存
     */
    private final LoadingCache<Long, IotDeviceRespDTO> deviceCaches = buildAsyncReloadingCache(
            CACHE_EXPIRE,
            new CacheLoader<>() {

                @Override
                public IotDeviceRespDTO load(Long id) {
                    CommonResult<IotDeviceRespDTO> result = deviceApi.getDevice(new IotDeviceGetReqDTO().setId(id));
                    IotDeviceRespDTO device = result.getCheckedData();
                    Assert.notNull(device, "设备({}) 不能为空", id);
                    // 相互缓存
                    deviceCaches2.put(new KeyValue<>(device.getProductKey(), device.getDeviceName()), device);
                    return device;
                }

            });

    /**
     * 通过 productKey + deviceName 查询设备的缓存
     */
    private final LoadingCache<KeyValue<String, String>, IotDeviceRespDTO> deviceCaches2 = buildAsyncReloadingCache(
            CACHE_EXPIRE,
            new CacheLoader<>() {

                @Override
                public IotDeviceRespDTO load(KeyValue<String, String> kv) {
                    CommonResult<IotDeviceRespDTO> result = deviceApi.getDevice(new IotDeviceGetReqDTO()
                            .setProductKey(kv.getKey()).setDeviceName(kv.getValue()));
                    IotDeviceRespDTO device = result.getCheckedData();
                    Assert.notNull(device, "设备({}/{}) 不能为空", kv.getKey(), kv.getValue());
                    // 相互缓存
                    deviceCaches.put(device.getId(), device);
                    return device;
                }
            });

    @Resource
    private IotDeviceCommonApi deviceApi;

    @Override
    public IotDeviceRespDTO getDeviceFromCache(String productKey, String deviceName) {
        return deviceCaches2.getUnchecked(new KeyValue<>(productKey, deviceName));
    }

    @Override
    public IotDeviceRespDTO getDeviceFromCache(Long id) {
        return deviceCaches.getUnchecked(id);
    }

}