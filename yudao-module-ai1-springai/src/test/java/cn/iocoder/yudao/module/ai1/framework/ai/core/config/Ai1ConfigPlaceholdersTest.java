package cn.iocoder.yudao.module.ai1.framework.ai.core.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.CONFIG_PLACEHOLDER_NOT_RESOLVED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

// TODO @AI：这个单测不需要噢
/**
 * {@link Ai1ConfigPlaceholders} 的单元测试
 *
 * @author 芋道源码
 */
public class Ai1ConfigPlaceholdersTest {

    @Test
    public void testResolve() {
        MockEnvironment environment = new MockEnvironment().withProperty("OPENAI_API_KEY", "sk-real");

        // 占位符解析为环境变量，普通值和空值原样返回
        assertEquals("sk-real", Ai1ConfigPlaceholders.resolve(environment, "${OPENAI_API_KEY}"));
        assertEquals("plain", Ai1ConfigPlaceholders.resolve(environment, "plain"));
        assertNull(Ai1ConfigPlaceholders.resolve(environment, null));

        // 环境变量不存在时失败，且不返回解析后的密钥
        assertServiceException(() -> Ai1ConfigPlaceholders.resolve(environment, "${MISSING}"),
                CONFIG_PLACEHOLDER_NOT_RESOLVED, "${MISSING}");
    }

}
