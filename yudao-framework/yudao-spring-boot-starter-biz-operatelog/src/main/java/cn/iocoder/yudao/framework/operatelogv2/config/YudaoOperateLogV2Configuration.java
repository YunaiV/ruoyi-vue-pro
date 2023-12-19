package cn.iocoder.yudao.framework.operatelogv2.config;

import cn.iocoder.yudao.framework.operatelogv2.core.aop.OperateLogV2Aspect;
import cn.iocoder.yudao.framework.operatelogv2.core.service.ILogRecordServiceImpl;
import com.mzt.logapi.service.ILogRecordService;
import com.mzt.logapi.starter.annotation.EnableLogRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * mzt-biz-log 配置类
 *
 * @author HUIHUI
 */
@EnableLogRecord(tenant = "") // 貌似用不上 tenant 这玩意给个空好啦
@AutoConfiguration
@Slf4j
public class YudaoOperateLogV2Configuration {

    @Bean
    @Primary
    public ILogRecordService iLogRecordServiceImpl() {
        return new ILogRecordServiceImpl();
    }

    @Bean
    public OperateLogV2Aspect operateLogV2Aspect() {
        return new OperateLogV2Aspect();
    }

}
