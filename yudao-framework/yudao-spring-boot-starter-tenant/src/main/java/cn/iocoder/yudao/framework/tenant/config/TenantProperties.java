package cn.iocoder.yudao.framework.tenant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

/**
 * 多租户配置
 *
 * @author 芋道源码
 */
@ConfigurationProperties(prefix = "yudao.tenant")
@Data
public class TenantProperties {

    /**
     * 需要多租户的表
     *
     * 由于多租户并不作为 yudao 项目的重点功能，更多是扩展性的功能，所以采用正向配置需要多租户的表。
     * 如果需要，你可以改成 ignoreTables 来取消部分不需要的表
     */
    private Set<String> tables;

}
