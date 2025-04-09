package cn.iocoder.yudao.framework.template.core.impl;

import cn.iocoder.yudao.framework.template.core.TemplateService;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

import static cn.iocoder.yudao.framework.common.enums.ErrorCodeConstants.GENERATE_CONTRACT_FAIL;
import static cn.iocoder.yudao.framework.common.enums.ErrorCodeConstants.GENERATE_CONTRACT_FAIL_PARSE;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Slf4j
@Getter
@Setter
public class TemplateServiceRedisImpl implements TemplateService {

    private static final String REDIS_KEY_PREFIX = "common:template:";
    private static final Duration CACHE_TTL = Duration.ofDays(1);

    private ResourcePatternResolver resourcePatternResolver;
    private RedisTemplate<String, byte[]> redisTemplate;
    private TemplateService self;

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
    public XWPFTemplate buildXWPDFTemplate(String path, Configure configure) {
        byte[] templateBytes = getTemplateBytesByPath(path);
        if (templateBytes == null || templateBytes.length == 0) {
            throw exception(GENERATE_CONTRACT_FAIL, path, "模板内容为空");
        }

        try (InputStream input = new ByteArrayInputStream(templateBytes)) {
            return XWPFTemplate.compile(input, configure);
        } catch (IOException e) {
            throw exception(GENERATE_CONTRACT_FAIL_PARSE, path, e.getMessage());
        }
    }

    @Override
    public XWPFTemplate reBuildXWPDFTemplate(String path, Configure configure) {
        refreshTemplateBytes(path);
        return buildXWPDFTemplate(path, configure);
    }

    @Override
    public byte[] getTemplateBytesByPath(String path) {
        String redisKey = REDIS_KEY_PREFIX + path;
        byte[] bytes = redisTemplate.opsForValue().get(redisKey);

        if (bytes != null && bytes.length > 0) {
            redisTemplate.expire(redisKey, CACHE_TTL);
            log.debug("模板缓存命中并续期 [{}]", path);
            return bytes;
        }

        log.debug("模板缓存未命中，加载并缓存 [{}]", path);
        Resource resource = resourcePatternResolver.getResource("classpath:" + path);
        bytes = getTemplateBytesFromResource(resource);
        if (bytes.length > 0) {
            redisTemplate.opsForValue().set(redisKey, bytes, CACHE_TTL);
        }
        return bytes;
    }

    @Override
    public byte[] refreshTemplateBytes(String path) {
        String redisKey = REDIS_KEY_PREFIX + path;
        Resource resource = resourcePatternResolver.getResource("classpath:" + path);
        byte[] bytes = getTemplateBytesFromResource(resource);
        redisTemplate.opsForValue().set(redisKey, bytes, CACHE_TTL);
        log.debug("模板缓存已刷新 [{}]", path);
        return bytes;
    }

    @Override
    public byte[] getTemplateBytes(Resource resource) {
        return getTemplateBytesFromResource(resource);
    }
}
