package cn.iocoder.yudao.framework.template.core.impl;

import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.template.config.TemplateConfigureFactory;
import cn.iocoder.yudao.framework.template.core.TemplateService;
import com.deepoove.poi.XWPFTemplate;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.GENERATE_CONTRACT_FAIL;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.GENERATE_CONTRACT_FAIL_PARSE;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Primary
@Slf4j
@Getter
@Setter
public class TemplateServiceRedisImpl implements TemplateService {

    private static final String REDIS_KEY_PREFIX = "common:template:";
    private static final Duration CACHE_TTL = Duration.ofDays(1);

    private RedisTemplate<String, byte[]> redisTemplate;
    private TemplateConfigureFactory configureFactory;

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

    //提取classes路径下的文件str
    private static String getRelativePath(Resource resource) throws IOException {
        String fullPath = resource.getURL().getPath();
        String keyPart = StrUtil.subAfter(fullPath, "classes/", true);
        return URLDecoder.decode(keyPart, StandardCharsets.UTF_8);
    }

    @Override
    public XWPFTemplate reBuildXWPDFTemplate(Resource resource) {
        refreshTemplateBytes(resource);
        return buildXWPDFTemplate(resource);
    }

    @Override
    public XWPFTemplate buildXWPDFTemplate(Resource resource) {
        byte[] templateBytes = getTemplateBytesByPath(resource);
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
    public byte[] getTemplateBytesByPath(Resource resource) {
        String redisKey = buildRedisKey(resource);
        byte[] bytes = redisTemplate.opsForValue().get(redisKey);

        if (bytes != null && bytes.length > 0) {
            redisTemplate.expire(redisKey, CACHE_TTL);
            log.debug("模板缓存命中并续期 [{}]", resource.getFilename());
            return bytes;
        }

        log.debug("模板缓存未命中，加载并缓存 [{}]", resource.getFilename());
        bytes = getTemplateBytesFromResource(resource);
        if (bytes.length > 0) {
            redisTemplate.opsForValue().set(redisKey, bytes, CACHE_TTL);
        }
        return bytes;
    }

    @Override
    public byte[] getTemplateBytes(Resource resource) {
        return getTemplateBytesFromResource(resource);
    }

    @Override
    public byte[] refreshTemplateBytes(Resource resource) {
        String redisKey = buildRedisKey(resource);
        byte[] bytes = getTemplateBytesFromResource(resource);
        redisTemplate.opsForValue().set(redisKey, bytes, CACHE_TTL);
        log.debug("模板缓存已刷新 [{}]", resource.getFilename());
        return bytes;
    }

    private String buildRedisKey(Resource resource) {
        try {
            String relativePath = getRelativePath(resource);
            return REDIS_KEY_PREFIX + URLDecoder.decode(relativePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("构建 Redis Key 失败，fallback 使用文件名: {}", resource.getFilename(), e);
            return REDIS_KEY_PREFIX + resource.getFilename();
        }
    }

}
