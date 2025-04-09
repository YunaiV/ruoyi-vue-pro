package cn.iocoder.yudao.framework.template.core;

import cn.iocoder.yudao.framework.redis.config.YudaoRedisAutoConfiguration;
import cn.iocoder.yudao.framework.template.config.TemplateConfigureFactory;
import cn.iocoder.yudao.framework.template.config.TemplateProperties;
import cn.iocoder.yudao.framework.template.core.impl.TemplateServiceImpl;
import cn.iocoder.yudao.framework.template.core.impl.TemplateServiceRedisImpl;
import cn.iocoder.yudao.framework.test.core.ut.BaseRedisUnitTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;

@Slf4j
@Disabled
@Import({TemplateServiceImpl.class, TemplateConfigureFactory.class, TemplateProperties.class, RedisTemplate.class, YudaoRedisAutoConfiguration.class,
    TemplateServiceRedisImpl.class, ResourcePatternResolver.class})
class TemplateServiceTest extends BaseRedisUnitTest {

    private static final String TEST_TEMPLATE_PATH = "template/test.docx";
    private static final String REDIS_KEY_PREFIX = "common:template:";
    @Autowired
    ResourceLoader resourceLoader;
    @Resource
    RedisTemplate<String, byte[]> byteArrayRedisTemplate;
    @Resource
    private TemplateService templateService;

    //    @Nested
    //    @Configuration
    //    class TestConfiguration {
    //        @Bean
    //        @ConditionalOnMissingBean
    //        public TemplateService templateService(ResourcePatternResolver resolver, RedisTemplate<String, byte[]> byteArrayRedisTemplate) {
    //            TemplateServiceRedisImpl service = new TemplateServiceRedisImpl();
    //            service.setResourcePatternResolver(resolver);
    //            //        service.setSelf(service); // 注入代理对象
    //            service.setRedisTemplate(byteArrayRedisTemplate);
    //            return service;
    //        }
    //        /**
    //         * 创建支持 byte[] 存储的 RedisTemplate
    //         */
    //        @Bean(name = "byteArrayRedisTemplate")
    //        public RedisTemplate<String, byte[]> byteArrayRedisTemplate() {
    //            RedisTemplate<String, byte[]> template = new RedisTemplate<>();
    //
    //            // key 使用字符串序列器
    //            template.setKeySerializer(RedisSerializer.string());
    //            template.setHashKeySerializer(RedisSerializer.string());
    //
    //            // value 使用原始 byte[] 序列器
    //            template.setValueSerializer(RedisSerializer.byteArray());
    //            template.setHashValueSerializer(RedisSerializer.byteArray());
    //
    //            return template;
    //        }
    //
    //        @Bean
    //        public ResourcePatternResolver resourcePatternResolver() {
    //            return new StaticApplicationContext();
    //    }

    @BeforeEach
    void setUp() {
        // 清空测试数据
        byteArrayRedisTemplate.delete(REDIS_KEY_PREFIX + TEST_TEMPLATE_PATH);
    }

    @Test
    void getTemplateBytes() throws IOException {
        // 准备测试数据
        ClassPathResource resource = new ClassPathResource(TEST_TEMPLATE_PATH);

        // 执行测试
        byte[] bytes = templateService.getTemplateBytes(resource);

        // 验证结果
        log.info("Template bytes: {}", bytes);
        log.info("Template bytes length: {}", bytes.length);
    }
    //
    //    @Test
    //    void buildXWPDFTemplate() {
    //        // 准备测试数据
    //        Configure configure = Configure.builder().build();
    //
    //        // 执行测试
    //        try (XWPFTemplate xwpfTemplate = templateService.buildXWPDFTemplate(TEST_TEMPLATE_PATH, configure)) {
    //            // 验证结果
    //            assertNotNull(xwpfTemplate);
    //            Configure config = xwpfTemplate.getConfig();
    //            log.info("Template config: {}", config);
    //        } catch (IOException e) {
    //            fail("Failed to build template", e);
    //        }
    //    }
    //
    //    @Test
    //    void reBuildXWPDFTemplate() {
    //        // 准备测试数据
    //        Configure configure = Configure.builder().build();
    //
    //        // 执行测试
    //        try (XWPFTemplate xwpfTemplate = templateService.reBuildXWPDFTemplate(TEST_TEMPLATE_PATH, configure)) {
    //            // 验证结果
    //            assertNotNull(xwpfTemplate);
    //            Configure config = xwpfTemplate.getConfig();
    //            log.info("Template config: {}", config);
    //        } catch (IOException e) {
    //            fail("Failed to rebuild template", e);
    //        }
    //    }
    //
    //    @Test
    //    void refreshTemplateBytes() {
    //        // 执行测试
    //        byte[] bytes = templateService.refreshTemplateBytes(TEST_TEMPLATE_PATH);
    //
    //        // 验证结果
    //        assertNotNull(bytes);
    //        assertTrue(bytes.length > 0);
    //        log.info("Refreshed template bytes length: {}", bytes.length);
    //    }
    //
    //    @Test
    //    void getTemplateBytesByPath() {
    //        // 执行测试
    //        byte[] bytes = templateService.getTemplateBytesByPath(TEST_TEMPLATE_PATH);
    //
    //        // 验证结果
    //        assertNotNull(bytes);
    //        assertTrue(bytes.length > 0);
    //        log.info("Template bytes from path length: {}", bytes.length);
    //    }}
}