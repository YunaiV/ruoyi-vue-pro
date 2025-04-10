package cn.iocoder.yudao.framework.template.core.impl;

import cn.iocoder.yudao.framework.template.config.TemplateConfigureFactory;
import cn.iocoder.yudao.framework.template.core.TemplateService;
import com.deepoove.poi.XWPFTemplate;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.GENERATE_CONTRACT_FAIL;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.GENERATE_CONTRACT_FAIL_PARSE;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Slf4j
@Getter
@Setter
public class TemplateServiceImpl implements TemplateService {

    private static final String REDIS_KEY_PREFIX = "common:template";

    private ResourcePatternResolver resourcePatternResolver;

    private TemplateService self;

    private TemplateConfigureFactory configureFactory;

    @Override
    public XWPFTemplate buildXWPDFTemplate(Resource resource) {
        byte[] templateBytes = self.getTemplateBytesByPath(resource);
        if (templateBytes == null || templateBytes.length == 0) {
            throw exception(GENERATE_CONTRACT_FAIL, resource.getFilename(), "模板内容为空");
        }

        try (InputStream input = new ByteArrayInputStream(templateBytes)) {
            return XWPFTemplate.compile(input, configureFactory.buildConfigure(resource));
        } catch (IOException e) {
            throw exception(GENERATE_CONTRACT_FAIL_PARSE, resource.getFilename(), e.getMessage());
        }
    }

    public static byte[] getTemplateBytesFromResource(Resource resource) {
        if (resource == null) {
            throw exception(GENERATE_CONTRACT_FAIL, "null", "资源对象不能为空");
        }

        try {
            if (!resource.exists()) {
                throw exception(GENERATE_CONTRACT_FAIL, resource.getFilename(), "模板文件不存在");
            }

            try (InputStream stream = resource.getInputStream()) {
                byte[] bytes = stream.readAllBytes();
                log.debug("模板加载成功: {} ({} bytes)", resource.getFilename(), bytes.length);
                return bytes;
            }
        } catch (IOException e) {
            throw exception(GENERATE_CONTRACT_FAIL_PARSE, resource.getFilename(), e.getMessage());
        }
    }

    @Override
    public XWPFTemplate reBuildXWPDFTemplate(Resource resource) {
        byte[] templateBytes = self.refreshTemplateBytes(resource);
        if (templateBytes == null || templateBytes.length == 0) {
            throw exception(GENERATE_CONTRACT_FAIL, resource.getFilename(), "模板内容为空");
        }

        try (InputStream input = new ByteArrayInputStream(templateBytes)) {
            return XWPFTemplate.compile(input, configureFactory.buildConfigure(resource));
        } catch (IOException e) {
            throw exception(GENERATE_CONTRACT_FAIL_PARSE, resource.getFilename(), e.getMessage());
        }
    }

    @Override
    @Cacheable(cacheNames = REDIS_KEY_PREFIX, key = "#resource", unless = "#result == null")
    public byte[] getTemplateBytesByPath(Resource resource) {
        return getTemplateBytesFromResource(resource);
    }

    @Override
    public byte[] getTemplateBytes(Resource resource) {
        return getTemplateBytesFromResource(resource);
    }

    @Override
    @CachePut(cacheNames = REDIS_KEY_PREFIX, key = "#resource", unless = "#result == null")
    public byte[] refreshTemplateBytes(Resource resource) {
        log.debug("重新模板预热: {}", resource.getFilename());
        return getTemplateBytesFromResource(resource);
    }
}

