package cn.iocoder.yudao.module.system.framework.bizlog.config;

import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.system.api.logger.OperateLogApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.framework.bizlog.service.AdminUserParseFunction;
import cn.iocoder.yudao.module.system.framework.bizlog.service.ILogRecordServiceImpl;
import com.mzt.logapi.beans.Operator;
import com.mzt.logapi.service.IOperatorGetService;
import com.mzt.logapi.starter.annotation.EnableLogRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;


/**
 * 使用 @Configuration 是因为 mzt-biz-log 的配置类是 @Configuration 的
 *
 * @author HUIHUI
 */
@Configuration(proxyBeanMethods = false)
@EnableLogRecord(tenant = "${yudao.info.base-package}")
@Slf4j
public class YudaoOperateLogV2Configuration {

    //======================= mzt-biz-log =======================

    @Bean
    public ILogRecordServiceImpl iLogRecordServiceImpl(OperateLogApi operateLogApi) {
        log.info("ILogRecordServiceImpl 初始化");
        return new ILogRecordServiceImpl(operateLogApi);
    }

    @Bean
    public IOperatorGetService operatorGetLoginUserIdService() {
        // 获取操作用户编号
        return () -> Optional.of(WebFrameworkUtils.getLoginUserId())
                .map(a -> {
                    Operator operator = new Operator();
                    operator.setOperatorId(a.toString());
                    return operator;
                })
                .orElseThrow(() -> new IllegalArgumentException("user is null"));
    }

    @Bean
    public AdminUserParseFunction adminUserParseFunction(AdminUserApi adminUserApi) {
        return new AdminUserParseFunction(adminUserApi);
    }

}
