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

    @Value("${erp.template.scan-path:purchase/order/}")
    private String templateScanPath;

    @Value("${erp.template.enable-preload:false}")
    private boolean enablePreload;
    @Autowired
    private ResourcePatternResolver resourcePatternResolver;

    private static final List<String> LOOP_POLICY_TAGS = List.of("products");

    private final Map<String, byte[]> templateCache = new ConcurrentHashMap<>();

    private final Configure configure;

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

    @EventListener(ApplicationReadyEvent.class)
    public void preloadTemplates() {
        // 默认跳过缓存
        if (!enablePreload) {
            return;
        }
        try {
            //扫描指定文件夹下所有 .docx 模板
            Resource[] resources = resourcePatternResolver.getResources("classpath:" + templateScanPath + "*.docx");
            log.info("检测到 {} 个模板文件：", resources.length);
            CompletableFuture.runAsync(() -> {
                log.info("[1] 开始加载 Word 模板...");
                long wordStart = System.currentTimeMillis();
                for (Resource resource : resources) {
                    try {
                        log.info("模板文件：{}", resource.getFilename());
                        String templateName = extractTemplateName(resource);
                        getTemplate(templateName);
                    } catch (Exception e) {
                        log.error("⚠️ Word 模板预热失败（已忽略）：{}", resource.getFilename(), e);
                    }
                }
                log.info("[1] Word 模板预热完成，耗时 {}ms", System.currentTimeMillis() - wordStart);
            }).thenRunAsync(() -> {
                for (Resource resource : resources) {
                    try {
                        String templateName = extractTemplateName(resource);
                        byte[] templateBytes = templateCache.get(templateName);
                        if (templateBytes != null) {
                            try (InputStream input = new ByteArrayInputStream(templateBytes)) {
                                long pdfStart = System.currentTimeMillis();
                                Document doc = new Document(input);
                                doc.save(new ByteArrayOutputStream(), SaveFormat.PDF);
                                log.info("[2] PDF 引擎预热完成 [{}]，耗时 {}ms", templateName, System.currentTimeMillis() - pdfStart);
                            }
                        } else {
                            log.warn("⚠️ PDF 引擎预热跳过：模板 [{}] 未成功加载", templateName);
                        }
                    } catch (Exception e) {
                        log.warn("⚠️ PDF 引擎预热失败", e);
                    }
                }
            });
        } catch (IOException e) {
            log.error("❌ 模板文件夹扫描失败", e);
        }
    }

    /**
     * 提取模板路径相对 classpath 的路径（用于缓存键）
     */
    private String extractTemplateName(Resource resource) throws IOException {
        String path = resource.getURL().getPath();
        String normalizedScanPath = templateScanPath.startsWith("/") ? templateScanPath : "/" + templateScanPath;
        int index = path.indexOf(normalizedScanPath);
        if (index == -1) {
            throw new IOException("无法解析模板路径，未包含扫描路径: " + templateScanPath);
        }
        return path.substring(index + 1); // 去掉前导斜杠
    }
}

