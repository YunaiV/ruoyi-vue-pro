package cn.iocoder.yudao.module.erp.config.purchase;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.plugin.table.LoopRowTableRenderPolicy;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 加载时间长-大概17S
 */
@Component
public class PurchaseOrderTemplateManager {

    @Autowired
    private ResourcePatternResolver resourcePatternResolver;

    private final Map<String, byte[]> templateCache = new ConcurrentHashMap<>();

    //采购合同模板预热
    public XWPFTemplate getTemplate(String templateName) {
        byte[] templateBytes = templateCache.computeIfAbsent(templateName, name -> {
            try (InputStream stream = resourcePatternResolver.getResource("classpath:" + templateName).getInputStream()) {
                return stream.readAllBytes();
            } catch (IOException e) {
//                throw exception(PURCHASE_ORDER_GENERATE_CONTRACT_FAIL,templateName,e.getMessage());
                throw new RuntimeException("加载模板失败: " + name, e);
            }
        });

        // 每次创建新的 InputStream 和 XWPFTemplate
        try (InputStream input = new ByteArrayInputStream(templateBytes)) {
            LoopRowTableRenderPolicy policy = new LoopRowTableRenderPolicy();
            Configure config = Configure.builder().bind("products", policy).build();
            return XWPFTemplate.compile(input, config);
        } catch (IOException e) {

            throw new RuntimeException("解析模板失败: " + templateName, e);
        }
    }


    @PostConstruct
    public void preloadDefaultTemplates() {
        getTemplate("purchase/order/采购合同模板.docx"); // 可按需预热多个模板
    }
}
