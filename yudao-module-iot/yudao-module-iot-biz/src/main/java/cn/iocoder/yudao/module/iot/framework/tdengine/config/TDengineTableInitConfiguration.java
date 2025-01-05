package cn.iocoder.yudao.module.iot.framework.tdengine.config;

import cn.iocoder.yudao.module.iot.service.device.IotDeviceLogDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * TDengine 表初始化的 Configuration
 *
 * @author alwayssuper
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
@Order(Integer.MAX_VALUE) // 保证在最后执行
public class TDengineTableInitConfiguration implements ApplicationRunner {

    private final IotDeviceLogDataService deviceLogService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            // 初始化设备日志表
            deviceLogService.initTDengineSTable();
            log.info("初始化 设备日志表 TDengine 表结构成功");
        } catch (Exception ex) {
            if (ex.getMessage().contains("Table already exists")) {
                log.info("TDengine 设备日志超级表已存在，跳过创建");
                return;
            }else{
                log.error("初始化 设备日志表 TDengine  表结构失败", ex);
            }
            throw ex;
        }
    }
}
