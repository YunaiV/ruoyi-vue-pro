package cn.iocoder.yudao.framework.template.config;

import com.deepoove.poi.config.ConfigureBuilder;

/**
 * word模板策略注册器
 *
 * @author wdy
 */
public interface TemplatePolicyRegistrar {
    void register(ConfigureBuilder builder);
}
