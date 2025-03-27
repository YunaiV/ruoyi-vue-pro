package cn.iocoder.yudao.module.erp.config.purchase;

import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.config.ConfigureBuilder;
import com.deepoove.poi.plugin.table.LoopRowTableRenderPolicy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.SrmErrorCodeConstants.PURCHASE_ORDER_GENERATE_CONTRACT_FAIL;
import static cn.iocoder.yudao.module.erp.enums.SrmErrorCodeConstants.PURCHASE_ORDER_GENERATE_CONTRACT_FAIL_PARSE;

@Slf4j
@Component
//@Profile("prod")
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
     * 启动后自动预加载模板（异步并行）
     */

    @EventListener(ApplicationReadyEvent.class)
    public void preloadTemplates() {
        log.info("开始预热 Word 模板：{}", preloadTemplateList);
        long wordStart = System.currentTimeMillis();
        preloadTemplateList.parallelStream().forEach(template -> {
            try {
                getTemplate(template);
            } catch (Exception e) {
                log.error("⚠️ 模板预热失败（已忽略）：{}", template, e);
            }
        });

        if (!preloadTemplateList.isEmpty()) {
            String firstTemplate = preloadTemplateList.get(0);
            if (templateCache.containsKey(firstTemplate)) {
                Executors.newSingleThreadExecutor().submit(() -> {
                    long start = System.currentTimeMillis();
                    try (InputStream input = new ByteArrayInputStream(templateCache.get(firstTemplate))) {
                        Document doc = new Document(input);
                        doc.save(new ByteArrayOutputStream(), SaveFormat.PDF);
                        log.info("✅ PDF 引擎预热完成，耗时 {}ms", System.currentTimeMillis() - start);
                    } catch (Exception e) {
                        log.warn("PDF 引擎预热失败", e);
                    }
                });
            }
        }
        log.info("Word 模板预热流程完成, 耗时{}ms", System.currentTimeMillis() - wordStart);
    }
}
