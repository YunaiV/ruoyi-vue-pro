package cn.iocoder.yudao.module.infra.sentineldemo;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Sentinel 限流熔断示例的配置类
 *
 * 1. 注册 {@link SentinelResourceAspect} 切面，让 @SentinelResource 注解生效。单体项目没有引入 Spring Cloud Alibaba，需要自己注册
 * 2. 启动时，从 classpath 的 sentinel 目录加载示例的限流、熔断规则
 *
 * @author 芋道源码
 */
@Configuration(value = "demoSentinelConfiguration", proxyBeanMethods = false)
@Slf4j
public class DemoSentinelConfiguration {

    /**
     * 限流规则的 JSON 文件，资源名与 DemoSentinelController 中 @SentinelResource 的 value 一致
     */
    private static final String FLOW_RULES_PATH = "sentinel/demo-flow-rules.json";
    /**
     * 熔断规则的 JSON 文件
     */
    private static final String DEGRADE_RULES_PATH = "sentinel/demo-degrade-rules.json";

    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }

    /**
     * 加载示例的规则：在所有单例 Bean 初始化之后、Web 服务器开始接收请求之前执行
     *
     * 注意，规则只保存在当前 JVM 的内存中：
     * 1. loadRules 会替换同一类型的全部规则，自己的业务有其它规则来源时，需要合并后再加载
     * 2. 通过 Sentinel Dashboard 修改的规则，重启后会恢复成 JSON 文件中的规则
     */
    @Bean
    public SmartInitializingSingleton demoSentinelRuleLoader() {
        return () -> {
            FlowRuleManager.loadRules(parseRules(FLOW_RULES_PATH, FlowRule.class));
            DegradeRuleManager.loadRules(parseRules(DEGRADE_RULES_PATH, DegradeRule.class));
            // 打印实际生效的规则数：不合法的规则（例如说 count 小于 0）会被 Sentinel 忽略，此时数量会少于 JSON 文件中的
            log.info("[demoSentinelRuleLoader][加载 Sentinel 规则完成，限流规则({}) 条，熔断规则({}) 条]",
                    FlowRuleManager.getRules().size(), DegradeRuleManager.getRules().size());
        };
    }

    private static <T> List<T> parseRules(String path, Class<T> clazz) {
        return JsonUtils.parseArray(ResourceUtil.readUtf8Str(path), clazz);
    }

}
