package cn.iocoder.yudao.module.bpm.framework.flowable.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

/**
 * BPM 流程模型发起 HTTP 请求的配置类：请求地址的域名 / IP 黑白名单
 *
 * 作用于流程模型中配置的 HTTP 请求：HTTP 请求触发器、HTTP 回调触发器、任务前置/后置通知、流程前置/后置通知、审批人监听器 HTTP。
 * 默认关闭，不做任何校验，与原有行为一致。如需开启，直接修改本类字段的默认值即可，例如说：
 *
 * <pre>
 * private Boolean enabled = true;
 * private Set&lt;String&gt; allowHosts = new HashSet&lt;&gt;(Set.of("erp.example.internal", "10.0.0.8"));
 * private Set&lt;String&gt; denyHosts = new HashSet&lt;&gt;(Set.of("169.254.169.254"));
 * </pre>
 *
 * 注意：只校验流程模型中填写的 URL 的域名 / IP，不解析 DNS，不能防御 DNS 重绑定、代理、重定向等场景，不是完整的 SSRF 防护
 *
 * @author 芋道源码
 */
@ConfigurationProperties(prefix = "yudao.bpm.http-request")
@Data
public class BpmHttpRequestProperties {

    /**
     * 是否开启黑白名单校验，默认关闭
     */
    private Boolean enabled = false;

    /**
     * 白名单：域名或 IP，忽略大小写后精确匹配，例如说 erp.example.internal、10.0.0.8
     *
     * 非空时，只允许请求列表中的域名或 IP；为空时，不额外限制
     */
    private Set<String> allowHosts = new HashSet<>();

    /**
     * 黑名单：域名或 IP，忽略大小写后精确匹配，例如说 169.254.169.254
     *
     * 优先于白名单判断，命中时拒绝请求
     */
    private Set<String> denyHosts = new HashSet<>();

}
