package cn.iocoder.yudao.framework.template.config;

import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.StrUtil;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.config.ConfigureBuilder;
import com.deepoove.poi.policy.RenderPolicy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 收集所有模块渲染 Word 的策略
 */
@Slf4j
@Configuration
@Getter
public class TemplateConfigFactory {

    /**
     * 本地缓存（key = classpath 相对路径）
     */
    private final Map<String, Map<String, RenderPolicy>> resourceTagPolicyCache = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private List<TemplateRegister> registers = List.of();


    public static String buildResourceKey(Resource resource) {
        try {
            if (resource instanceof ClassPathResource classPathResource) {
                return classPathResource.getPath();
            }

            String fullPath = resource.getURL().getPath();
            String keyPart = StrUtil.subAfter(fullPath, "classes/", true);
            return URLDecoder.decode(keyPart, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("构建资源 Key 失败，fallback 使用文件名: {}", resource, e);
            return resource.getFilename();
        }
    }

    public Map<String, Map<String, RenderPolicy>> buildResourceTagPolicyMap() {
        if (!resourceTagPolicyCache.isEmpty()) {
            return resourceTagPolicyCache;
        }

        for (TemplateRegister register : registers) {
            List<TemplatePolicy> policies = register.getTemplatePolicies();
            if (policies == null || policies.isEmpty()) continue;

            for (TemplatePolicy policy : policies) {
                Resource resource = policy.getResource();
                if (resource == null) continue;

                String resourceKey = buildResourceKey(resource);
                if (StrUtil.isBlank(resourceKey)) continue;

                Map<String, RenderPolicy> tagPolicyMap =
                    resourceTagPolicyCache.computeIfAbsent(resourceKey, key -> new HashMap<>());

                if (policy.getPolicies() != null) {
                    for (Map<String, RenderPolicy> policyEntry : policy.getPolicies()) {
                        tagPolicyMap.putAll(policyEntry);
                    }
                }
            }
        }

        log.debug("构建模板策略绑定关系完成，共识别 {} 个模板", resourceTagPolicyCache.size());
        return resourceTagPolicyCache;
    }

    public Configure buildConfigure(Resource resource) {
        ConfigureBuilder builder = Configure.builder();
        String key = buildResourceKey(resource);
        Map<String, RenderPolicy> tagPolicyMap = buildResourceTagPolicyMap().get(key);
        if (tagPolicyMap != null && !tagPolicyMap.isEmpty()) {
            tagPolicyMap.forEach(builder::bind);
        }
        return builder.build();
    }

}