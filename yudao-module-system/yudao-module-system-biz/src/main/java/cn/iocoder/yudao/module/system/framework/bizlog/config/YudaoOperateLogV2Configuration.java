package cn.iocoder.yudao.module.system.framework.bizlog.config;

import com.mzt.logapi.starter.annotation.EnableLogRecord;
import org.springframework.context.annotation.Configuration;


/**
 *
 *
 * @author HUIHUI
 */
@Configuration(proxyBeanMethods = false)
@EnableLogRecord(tenant = "${yudao.info.base-package}")
public class YudaoOperateLogV2Configuration {

}
