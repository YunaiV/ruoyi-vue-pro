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
import java.util.Optional;
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

    /**
     * 获取模板
     *
     * @param path 相对路径文件 purchase/order/采购合同模板.docx
     * @return XWPFTemplate
     */
    public XWPFTemplate getTemplate(String path) {
        byte[] templateBytes = templateCache.computeIfAbsent(path, p -> {
            try {
                Resource resource = resourcePatternResolver.getResource("classpath:" + p);
                if (!resource.exists()) {
                    throw exception(PURCHASE_ORDER_GENERATE_CONTRACT_FAIL, p, "模板文件不存在");
                }
                try (InputStream stream = resource.getInputStream()) {
                    byte[] bytes = stream.readAllBytes();
                    log.info("采购模板加载成功并缓存: {}", p);
                    return bytes;
                }
            } catch (IOException e) {
                throw exception(PURCHASE_ORDER_GENERATE_CONTRACT_FAIL, p, e.getMessage());
            }
        });

        try (InputStream input = new ByteArrayInputStream(templateBytes)) {
            return XWPFTemplate.compile(input, configure);
        } catch (IOException e) {
            throw exception(PURCHASE_ORDER_GENERATE_CONTRACT_FAIL_PARSE, path, e.getMessage());
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void preloadTemplates() {
        if (!enablePreload) {
            return;
        }
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:" + templateScanPath + "*.docx");
            log.info("检测到 {} 个模板文件：", resources.length);

            CompletableFuture.runAsync(() -> {
                log.info("[1] 开始加载 Word 模板...");
                long wordStart = System.currentTimeMillis();
                //获取文件名列表
                for (org.springframework.core.io.Resource resource : resources) {
                    try {
                        Optional.ofNullable(resource.getFilename()).ifPresent(fileName -> {
                            if (fileName.endsWith(".docx")) {
                                getTemplate(templateScanPath + fileName);
                            }
                        });
                    } catch (Exception e) {
                        log.error("⚠️ Word 模板预热失败（已忽略）：{}", resource.getFilename(), e);
                    }
                }
                log.info("[1] Word 模板预热完成，耗时 {}ms", System.currentTimeMillis() - wordStart);
            }).thenRunAsync(() -> {
                for (Resource resource : resources) {
                    try {
                        byte[] templateBytes = templateCache.get(templateScanPath + resource.getFilename());
                        if (templateBytes != null) {
                            try (InputStream input = new ByteArrayInputStream(templateBytes)) {
                                long pdfStart = System.currentTimeMillis();
                                Document doc = new Document(input);
                                doc.save(new ByteArrayOutputStream(), SaveFormat.PDF);
                                log.info("[2] PDF 引擎预热完成 [{}]，耗时 {}ms", resource.getFilename(), System.currentTimeMillis() - pdfStart);
                            }
                        } else {
                            log.warn("⚠️ PDF 引擎预热跳过：模板 [{}] 未成功加载", resource.getFilename());
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


}

