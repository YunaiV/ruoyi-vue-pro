package cn.iocoder.yudao.module.iot.framework.storage.config;

import cn.iocoder.yudao.module.iot.framework.storage.core.enums.IotStorageTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * IoT 时序数据存储的配置项
 *
 * @author 芋道源码
 */
@ConfigurationProperties(prefix = IotStorageProperties.PREFIX)
@Validated
@Data
public class IotStorageProperties {

    public static final String PREFIX = "yudao.iot.storage";

    /**
     * 存储类型，同时决定属性历史、消息日志的存储后端
     *
     * 写错值时，配置绑定失败，应用启动失败
     */
    @NotNull(message = "IoT 存储类型不能为空")
    private IotStorageTypeEnum type = IotStorageTypeEnum.TDENGINE;

}
