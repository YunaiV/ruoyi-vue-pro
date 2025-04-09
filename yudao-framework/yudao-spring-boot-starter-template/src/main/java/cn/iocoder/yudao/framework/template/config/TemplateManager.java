package cn.iocoder.yudao.framework.template.config;

import cn.iocoder.yudao.framework.template.core.TemplateService;
import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import com.deepoove.poi.XWPFTemplate;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.scheduling.annotation.Async;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
@Setter
@Getter
@ConditionalOnProperty(prefix = "template", value = "enable-preload", havingValue = "true")
public class TemplateManager {

    @Autowired
    private TemplateConfigureFactory configureFactory;
    @Autowired
    private TemplateService templateService;
    @Autowired
    private ResourcePatternResolver resourcePatternResolver;
    @Autowired
    private TemplateProperties templateProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void preloadTemplatesOnStartup() {
        log.info("启动模板预热任务...");
        preloadWordAndPdfTemplates();
    }

    @Async
    public void preloadWordAndPdfTemplates() {
        long start = System.currentTimeMillis();
        var configure = configureFactory.build();

        configureFactory.getRegistrars().forEach(registrar -> {
            if (!registrar.enablePreload()) {
                return;
            }

            for (String path : registrar.scanPath()) {
                try {
                    Resource[] resources = resourcePatternResolver.getResources("classpath:" + path + "*.docx");
                    for (Resource resource : resources) {
                        String classpathPath = path + resource.getFilename();
                        preloadSingleWordTemplate(classpathPath, configure);
                        preloadSinglePdfTemplate(resource, resource.getFilename());
                    }
                } catch (IOException e) {
                    log.warn("模板加载失败 [{}]", path, e);
                }
            }
        });

        log.info("全部模板预热完成，耗时 {}ms", System.currentTimeMillis() - start);
    }

    private void preloadSingleWordTemplate(String classpathPath, com.deepoove.poi.config.Configure configure) {
        try (XWPFTemplate ignored = templateService.reBuildXWPDFTemplate(classpathPath, configure)) {
            log.info("Word 模板预热成功 [{}]", classpathPath);
        } catch (Exception e) {
            log.warn("Word 模板预热失败 [{}]: {}", classpathPath, e.getMessage());
            log.debug("详细错误信息：", e);
        }
    }

    private void preloadSinglePdfTemplate(Resource resource, String fileName) {
        try {
            byte[] templateBytes = templateService.getTemplateBytes(resource);
            if (templateBytes == null || templateBytes.length == 0) {
                log.warn("跳过 PDF 预热，模板 [{}] 加载失败或为空", fileName);
                return;
            }

            try (InputStream input = new ByteArrayInputStream(templateBytes)) {
                Document doc = new Document(input);
                doc.save(new ByteArrayOutputStream(), SaveFormat.PDF);
                log.info("PDF 引擎预热成功 [{}]", fileName);
            }

        } catch (Exception e) {
            log.warn("PDF 模板预热失败 [{}]: {}", fileName, e.getMessage());
            log.debug("详细错误信息：", e);
        }
    }

}