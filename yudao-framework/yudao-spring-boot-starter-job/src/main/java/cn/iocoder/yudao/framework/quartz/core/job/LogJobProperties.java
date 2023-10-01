package cn.iocoder.yudao.framework.quartz.core.job;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

// TODO @j-sentinel：这个配置类，先暂时不做，每个 Job 里面定一个静态类。其实不是所有的变量，都需要配置化，因为它本身基本也不会改动。
/**
 * @Author: j-sentinel
 * @Date: 2023/9/30 16:17
 */
@Data
@ConfigurationProperties(prefix = "yudao.clean-job")
public class LogJobProperties {

    private int accessRetainDay = 7;

    private int errorRetainDay = 8;

    private int jobCleanRetainDay = 7;

}
