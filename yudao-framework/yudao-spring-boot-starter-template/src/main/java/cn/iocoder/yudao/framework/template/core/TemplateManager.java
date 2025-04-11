package cn.iocoder.yudao.framework.template.core;

import cn.iocoder.yudao.framework.template.config.TemplateConfigFactory;
import cn.iocoder.yudao.framework.template.config.TemplatePolicy;
import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import com.deepoove.poi.XWPFTemplate;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Optional;

@Slf4j
@Configuration
@Setter
@Getter
public class TemplateManager {

    @Autowired
    private TemplateConfigFactory configureFactory;
    @Autowired
    private TemplateService templateService;


    @EventListener(ApplicationReadyEvent.class)
    public void preloadTemplatesOnStartup() {
        log.info("TemplateRegister 策略实例数：{}", configureFactory.getRegisters().size());
        log.info("开始执行模板预热任务...");
        preloadWordAndPdfTemplates();
    }

    @Async
    public void preloadWordAndPdfTemplates() {
        long start = System.currentTimeMillis();
        Optional.ofNullable(configureFactory.getRegisters()).ifPresent(registers -> registers.forEach(
            register -> register.getTemplatePolicies().stream().filter(TemplatePolicy::getEnablePreload).forEach(TemplatePolicy -> {
                preloadSingleWordTemplate(TemplatePolicy.getResource());
                preloadSinglePdfTemplate(TemplatePolicy.getResource());
            })));
        log.info("全部模板预热完成，耗时 {}ms", System.currentTimeMillis() - start);
    }

    private void preloadSingleWordTemplate(Resource resource) {
        try (XWPFTemplate ignored = templateService.rebuildXWPDFTemplate(resource)) {
            log.info("Word 模板预热成功 [{}]", resource.getFilename());
        } catch (Exception e) {
            log.warn("Word 模板预热失败 [{}]: {}", resource, e.getMessage());
            log.debug("详细错误信息：", e);
        }
    }

    private void preloadSinglePdfTemplate(Resource resource) {
        try {
            byte[] templateBytes = templateService.getTemplateBytes(resource);
            if (templateBytes == null || templateBytes.length == 0) {
                log.warn("跳过 PDF 预热，模板 [{}] 加载失败或为空", resource.getFilename());
                return;
            }
            try (InputStream input = new ByteArrayInputStream(templateBytes)) {
                Document doc = new Document(input);
                doc.save(new ByteArrayOutputStream(), SaveFormat.PDF);
                log.info("PDF 引擎预热成功 [{}]", resource.getFilename());
            }
        } catch (Exception e) {
            log.warn("PDF 模板预热失败 [{}]: {}", resource.getFilename(), e.getMessage());
            log.debug("详细错误信息：", e);
        }
    }
}