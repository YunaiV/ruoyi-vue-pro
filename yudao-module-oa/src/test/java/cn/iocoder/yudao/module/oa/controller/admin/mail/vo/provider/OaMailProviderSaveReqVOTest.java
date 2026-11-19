package cn.iocoder.yudao.module.oa.controller.admin.mail.vo.provider;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// TODO @AI：VO 不写单测；
/**
 * 邮件服务器主机名校验测试
 *
 * @author 芋道源码
 */
class OaMailProviderSaveReqVOTest {

    @Test
    void testHostValidation_lengthAndFormatAreIndependent() {
        OaMailProviderSaveReqVO.ConnectionConfig config = new OaMailProviderSaveReqVO.ConnectionConfig();
        config.setHost("a.".repeat(10_000) + "a").setPort(993).setSslEnable(true).setStarttlsEnable(false);
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var violations = factory.getValidator().validate(config);
            assertEquals(1, violations.size());
            assertEquals("host", violations.iterator().next().getPropertyPath().toString());
        }
        assertFalse(config.setHost("a.".repeat(10_000) + "-a").isHostValid());
        assertTrue(config.setHost("imap.example.com.").isHostValid());
        assertTrue(config.setHost("imap.example.com").isHostValid());
        assertTrue(config.setHost("127.0.0.1").isHostValid());
        assertTrue(config.setHost("2001:db8::1").isHostValid());
    }

}
