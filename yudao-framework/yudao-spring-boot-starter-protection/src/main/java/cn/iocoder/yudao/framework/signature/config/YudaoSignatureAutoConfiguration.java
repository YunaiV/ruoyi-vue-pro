package cn.iocoder.yudao.framework.signature.config;

import cn.iocoder.yudao.framework.redis.config.YudaoRedisAutoConfiguration;
import cn.iocoder.yudao.framework.signature.core.aop.SignatureAspect;
import cn.iocoder.yudao.framework.signature.core.redis.SignatureRedisDAO;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author Zhougang
 */
@AutoConfiguration(after = YudaoRedisAutoConfiguration.class)
public class YudaoSignatureAutoConfiguration {

    @Bean
    public SignatureAspect signatureAspect(SignatureRedisDAO signatureRedisDAO) {
        return new SignatureAspect(signatureRedisDAO);
    }

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public SignatureRedisDAO signatureRedisDAO(StringRedisTemplate stringRedisTemplate) {
        return new SignatureRedisDAO(stringRedisTemplate);
    }

}
