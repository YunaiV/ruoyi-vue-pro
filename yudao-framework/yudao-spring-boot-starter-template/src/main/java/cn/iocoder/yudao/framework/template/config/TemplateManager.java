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
import java.util.ArrayList;
import java.util.List;

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
        log.info("开始执行模板预热任务...");
        preloadTemplatesAsync();
    }

    @Async
    public void preloadTemplatesAsync() {
        try {
            List<String> classpathPaths = loadAllClasspathDocxPaths(templateProperties.getScanPath());
            if (classpathPaths.isEmpty()) {
                log.warn("未找到任何模板文件，请检查配置 template.scan-path");
                return;
            }

            log.info("共检测到 {} 个模板文件", classpathPaths.size());
            preloadWordAndPdfTemplates(classpathPaths);

        } catch (IOException e) {
            log.error("模板文件夹扫描失败", e);
        } catch (Throwable t) {
            log.error("模板预热任务执行异常", t);
        }
    }

    private List<String> loadAllClasspathDocxPaths(List<String> scanPaths) throws IOException {
        List<String> result = new ArrayList<>();
        for (String basePath : scanPaths) {
            Resource[] resources = resourcePatternResolver.getResources("classpath:" + basePath + "*.docx");
            for (Resource resource : resources) {
                result.add(basePath + resource.getFilename()); // classpath 相对路径
            }
        }
        return result;
    }

    private void preloadWordAndPdfTemplates(List<String> classpathPaths) {
        long start = System.currentTimeMillis();
        var configure = configureFactory.build();

        for (String classpathPath : classpathPaths) {
            preloadSingleWordTemplate(classpathPath, configure);

            Resource resource = resourcePatternResolver.getResource("classpath:" + classpathPath);
            preloadSinglePdfTemplate(resource, extractFileNameFromPath(classpathPath));
        }

        log.info("全部模板预热完成，耗时 {}ms", System.currentTimeMillis() - start);
    }

    private void preloadSingleWordTemplate(String classpathPath, com.deepoove.poi.config.Configure configure) {
        try (XWPFTemplate ignored = templateService.buildXWPDFTemplate(classpathPath, configure)) {
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

    private String extractFileNameFromPath(String path) {
        int index = path.lastIndexOf('/');
        return index != -1 ? path.substring(index + 1) : path;
    }
}
