package cn.iocoder.yudao.module.iot.script.context;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 设备脚本上下文，提供设备相关的上下文信息
 */
@Slf4j
public class DeviceScriptContext extends DefaultScriptContext {

    /**
     * 产品 Key
     */
    @Getter
    private String productKey;

    /**
     * 设备名称
     */
    @Getter
    private String deviceName;

    /**
     * 设备属性数据缓存
     */
    private Map<String, Object> properties;

    /**
     * 使用产品 Key 和设备名称初始化上下文
     *
     * @param productKey 产品 Key
     * @param deviceName 设备名称，可以为 null
     * @return 当前上下文实例，用于链式调用
     */
    public DeviceScriptContext withDeviceInfo(String productKey, String deviceName) {
        this.productKey = productKey;
        this.deviceName = deviceName;

        // 添加到参数中，便于脚本访问
        setParameter("productKey", productKey);
        if (StrUtil.isNotEmpty(deviceName)) {
            setParameter("deviceName", deviceName);
        }
        return this;
    }

    /**
     * 设置设备属性数据
     *
     * @param properties 属性数据
     * @return 当前上下文实例，用于链式调用
     */
    public DeviceScriptContext withProperties(Map<String, Object> properties) {
        this.properties = properties;
        if (MapUtil.isNotEmpty(properties)) {
            setParameter("properties", properties);
        }
        return this;
    }

    /**
     * 获取设备属性值
     *
     * @param key 属性标识符
     * @return 属性值
     */
    public Object getProperty(String key) {
        if (MapUtil.isEmpty(properties)) {
            return null;
        }
        return properties.get(key);
    }

    /**
     * 设置设备属性值
     *
     * @param key   属性标识符
     * @param value 属性值
     */
    public void setProperty(String key, Object value) {
        if (this.properties == null) {
            this.properties = MapUtil.newHashMap();
            setParameter("properties", this.properties);
        }
        this.properties.put(key, value);
    }
}