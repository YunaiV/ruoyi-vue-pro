package cn.iocoder.yudao.module.srm.config.purchase;

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
import org.springframework.context.annotation.Profile;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.srm.enums.SrmErrorCodeConstants.PURCHASE_ORDER_GENERATE_CONTRACT_FAIL;
import static cn.iocoder.yudao.module.srm.enums.SrmErrorCodeConstants.PURCHASE_ORDER_GENERATE_CONTRACT_FAIL_PARSE;

@Slf4j
@Component
public class PurchaseOrderTemplateManager {

    @Autowired
    private ResourcePatternResolver resourcePatternResolver;

    private static final List<String> LOOP_POLICY_TAGS = List.of("products");

    private final Map<String, byte[]> templateCache = new ConcurrentHashMap<>();

    private final Configure configure;

    @Value("#{'${erp.template.preload-list:purchase/order/采购合同模板.docx}'.split(',')}")
    private List<String> preloadTemplateList;

    public PurchaseOrderTemplateManager() {
        ConfigureBuilder builder = Configure.builder();
        LOOP_POLICY_TAGS.forEach(tag -> builder.bind(tag, new LoopRowTableRenderPolicy()));
        this.configure = builder.build();
    }

    public XWPFTemplate getTemplate(String templateName) {
        byte[] templateBytes = templateCache.computeIfAbsent(templateName, name -> {
            try {
                Resource resource = resourcePatternResolver.getResource("classpath:" + name);
                if (!resource.exists()) {
                    throw exception(PURCHASE_ORDER_GENERATE_CONTRACT_FAIL, name, "模板文件不存在");
                }
                try (InputStream stream = resource.getInputStream()) {
                    byte[] bytes = stream.readAllBytes();
                    log.info("✅ 采购模板加载成功并缓存: {}", name);
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

    @Profile("prod")
    @EventListener(ApplicationReadyEvent.class)
    public void preloadTemplates() {
        log.info("🚀 启动后开始异步预热 Word 模板与 PDF 引擎：{}", preloadTemplateList);

        CompletableFuture.runAsync(() -> {
            log.info("👉 [1] 开始加载 Word 模板...");
            long wordStart = System.currentTimeMillis();
            for (String template : preloadTemplateList) {
                try {
                    getTemplate(template);
                } catch (Exception e) {
                    log.error("⚠️ Word 模板预热失败（已忽略）：{}", template, e);
                }
            }
            log.info("✅ [1] Word 模板预热完成，耗时 {}ms", System.currentTimeMillis() - wordStart);
        }).thenRunAsync(() -> {
            if (preloadTemplateList.isEmpty()) {
                return;
            }
            for (String templateName : preloadTemplateList) {
                byte[] templateBytes = templateCache.get(templateName);

                if (templateBytes != null) {
                    try (InputStream input = new ByteArrayInputStream(templateBytes)) {
                        long pdfStart = System.currentTimeMillis();
                        Document doc = new Document(input);
                        doc.save(new ByteArrayOutputStream(), SaveFormat.PDF);
                        log.info("✅ [2] PDF 引擎预热完成，耗时 {}ms", System.currentTimeMillis() - pdfStart);
                    } catch (Exception e) {
                        log.warn("⚠️ PDF 引擎预热失败", e);
                    }
                } else {
                    log.warn("⚠️ PDF 引擎预热跳过：模板 [{}] 未成功加载", templateName);
                }
            }

        });

        log.info("✅ Word/PDF 预热任务已提交（异步中），主线程继续启动流程");
    }

}
