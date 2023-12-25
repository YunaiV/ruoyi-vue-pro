package cn.iocoder.yudao.framework.operatelogv2.config;

import cn.iocoder.yudao.framework.operatelogv2.core.aop.OperateLogV2Aspect;
import cn.iocoder.yudao.framework.operatelogv2.core.service.ILogRecordServiceImpl;
import com.mzt.logapi.service.ILogRecordService;
import com.mzt.logapi.starter.annotation.EnableLogRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

// TODO @puhui999：思考了下，为了减少 starter 数量，在 security 组件里，增加一个 core/operatelog 包，然后 YudaoOperateLogV2Configuration 搞一个过去；
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
