package cn.iocoder.yudao.module.iot.framework.tdengine.config;

import cn.iocoder.yudao.module.iot.service.device.data.IotDeviceLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * TDengine 表初始化的 Configuration
 *
 * @author alwayssuper
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TDengineTableInitRunner implements ApplicationRunner {

    private final IotDeviceLogService deviceLogService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            // 初始化设备日志表
            deviceLogService.defineDeviceLog();
        } catch (Exception ex) {
            // 初始化失败时打印错误日志并退出系统
            log.error("[run][TDengine初始化设备日志表结构失败，系统无法正常运行，即将退出]", ex);
            System.exit(1);
        }
    }

}
