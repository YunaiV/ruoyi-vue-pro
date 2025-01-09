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
@Order
public class TDengineTableInitConfiguration implements ApplicationRunner {

    private final IotDeviceLogDataService deviceLogService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            // 初始化设备日志表
            deviceLogService.initTDengineSTable();
            // TODO @super：这个日志，是不是不用打，不然重复啦！！！
            log.info("[run]初始化 设备日志表 TDengine 表结构成功");
        } catch (Exception ex) {
            // TODO @super：初始化失败，打印 error 日志，退出系统。。不然跑起来，就初始啦！！！
            if (ex.getMessage().contains("Table already exists")) {
                log.info("TDengine 设备日志超级表已存在，跳过创建");
                return;
            } else{
                log.error("初始化 设备日志表 TDengine  表结构失败", ex);
            }
            throw ex;
        }
    }

}
