package cn.iocoder.yudao.module.erp.config.purchase;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.config.ConfigureBuilder;
import com.deepoove.poi.plugin.table.LoopRowTableRenderPolicy;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.SrmErrorCodeConstants.PURCHASE_ORDER_GENERATE_CONTRACT_FAIL;
import static cn.iocoder.yudao.module.erp.enums.SrmErrorCodeConstants.PURCHASE_ORDER_GENERATE_CONTRACT_FAIL_PARSE;

@Slf4j
@Component
public class PurchaseOrderTemplateManager {

    @Autowired
    private ResourcePatternResolver resourcePatternResolver;
    // 多策略标签：products, services, packages
    private static final List<String> LOOP_POLICY_TAGS = List.of("products");

    private final Map<String, byte[]> templateCache = new ConcurrentHashMap<>();

    private final Configure configure;

    // 支持多个模板预热（可配置）
    @Value("#{'${erp.template.preload-list:purchase/order/采购合同模板.docx}'.split(',')}")
    private List<String> preloadTemplateList;

    //动态绑定多个标签策略
    public PurchaseOrderTemplateManager() {
        ConfigureBuilder builder = Configure.builder();
        LOOP_POLICY_TAGS.forEach(tag -> builder.bind(tag, new LoopRowTableRenderPolicy()));
        this.configure = builder.build();
    }

    /**
     * 获取采购合同模板
     *
     * @param templateName 模板文件路径，如 "purchase/order/采购合同模板.docx"
     */
    public XWPFTemplate getTemplate(String templateName) {
        byte[] templateBytes = templateCache.computeIfAbsent(templateName, name -> {
            try {
                Resource resource = resourcePatternResolver.getResource("classpath:" + name);
                if (!resource.exists()) {
                    throw exception(PURCHASE_ORDER_GENERATE_CONTRACT_FAIL, name, "模板文件不存在");
                }
                try (InputStream stream = resource.getInputStream()) {
                    byte[] bytes = stream.readAllBytes();
                    log.info("采购模板加载成功并缓存: {}", name);
                    return bytes;
                }
            } catch (IOException e) {
                throw exception(PURCHASE_ORDER_GENERATE_CONTRACT_FAIL, name, e.getMessage());
            }
        });

        try (InputStream input = new ByteArrayInputStream(templateBytes)) {
            return XWPFTemplate.compile(input, configure);
        } catch (IOException e) {
            throw exception(PURCHASE_ORDER_GENERATE_CONTRACT_FAIL_PARSE, templateName, e.getMessage());
        }
    }

    /**
     * 应用启动时预加载模板（支持多个）
     */
    @PostConstruct
    public void preloadDefaultTemplates() {
        preloadTemplateList.parallelStream().forEach(templateName -> {
            try {
                getTemplate(templateName);
            } catch (Exception e) {
                log.error("预加载采购合同模板失败: {}", templateName, e);
            }
        });
    }
}
