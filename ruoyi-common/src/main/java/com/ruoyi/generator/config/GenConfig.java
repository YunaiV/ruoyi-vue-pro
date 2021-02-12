package com.ruoyi.generator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * 读取代码生成相关配置
 *
 * @author ruoyi
 */
@Component
@ConfigurationProperties(prefix = "gen")
@PropertySource(value = { "classpath:generator.yml" })
public class GenConfig {

    /** 作者 */
    public static String author;

    /** 生成包路径 */
    public static String packageName;

    /** 自动去除表前缀，默认是false */
    public static boolean autoRemovePre;

    /** 表前缀(类名不会包含表前缀) */
    public static String tablePrefix;

}
