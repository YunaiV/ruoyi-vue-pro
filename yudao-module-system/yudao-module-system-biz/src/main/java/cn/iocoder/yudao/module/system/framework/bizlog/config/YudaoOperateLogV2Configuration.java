package cn.iocoder.yudao.module.system.framework.bizlog.config;

import com.mzt.logapi.starter.annotation.EnableLogRecord;
import org.springframework.context.annotation.Configuration;

/**
 * mzt-biz-log 配置类
 *
 * @author HUIHUI
 */
@Configuration(proxyBeanMethods = false)
@EnableLogRecord(tenant = "") // 貌似用不上 tenant 这玩意给个空好啦
public class YudaoOperateLogV2Configuration {

}
