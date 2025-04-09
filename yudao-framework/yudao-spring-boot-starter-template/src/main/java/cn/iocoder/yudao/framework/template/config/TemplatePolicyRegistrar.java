package cn.iocoder.yudao.framework.template.config;

import com.deepoove.poi.config.ConfigureBuilder;

import java.util.Collection;

/**
 * word模板策略注册器
 *
 * @author wdy
 */
public interface TemplatePolicyRegistrar {

    /**
     * word编译策略
     *
     * @param builder 配置构建器
     */
    void register(ConfigureBuilder builder);

    /**
     * 是否启用模板预热
     */
    boolean enablePreload();

    /**
     * 模板扫描路径（支持多个 classpath 目录）
     */
    Collection<String> scanPath();
}
