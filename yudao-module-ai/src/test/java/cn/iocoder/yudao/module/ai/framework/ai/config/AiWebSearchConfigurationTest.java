package cn.iocoder.yudao.module.ai.framework.ai.config;

import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.baidu.AiBaiduWebSearchClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.bocha.AiBoChaWebSearchClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.deepseek.AiDeepSeekWebSearchClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.parallel.AiParallelWebSearchClient;
import cn.iocoder.yudao.module.ai.tool.function.DirectoryListToolFunction;
import cn.iocoder.yudao.module.ai.tool.function.UserProfileQueryToolFunction;
import cn.iocoder.yudao.module.ai.tool.function.WeatherQueryToolFunction;
import cn.iocoder.yudao.module.ai.tool.method.PersonServiceImpl;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AiWebSearchConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(AiAutoConfiguration.class)
            .withBean(PersonServiceImpl.class)
            .withBean(DirectoryListToolFunction.class)
            .withBean(WeatherQueryToolFunction.class)
            .withBean(UserProfileQueryToolFunction.class)
            .withBean(AdminUserApi.class, () -> mock(AdminUserApi.class));

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"sk-xxxx"})
    void testWebSearchWithoutCredentials(String apiKey) {
        ApplicationContextRunner runner = contextRunner.withPropertyValues("yudao.ai.web-search.enable=true");
        if (apiKey != null) {
            runner = runner.withPropertyValues("yudao.ai.web-search.api-key=" + apiKey);
        }
        runner.run(context -> assertThat(context).hasSingleBean(AiParallelWebSearchClient.class));
    }

    @Test
    void testWebSearchWithKeyOnly() {
        assertProvider(AiDeepSeekWebSearchClient.class, "yudao.ai.web-search.api-key=test-key");
    }

    @Test
    void testExplicitProviderTakesPrecedence() {
        assertProvider(AiDeepSeekWebSearchClient.class, "yudao.ai.web-search.platform=deepseek");
        assertProvider(AiBoChaWebSearchClient.class, "yudao.ai.web-search.platform=bocha");
        assertProvider(AiBaiduWebSearchClient.class, "yudao.ai.web-search.platform=baidu");
        assertProvider(AiParallelWebSearchClient.class, "yudao.ai.web-search.platform=parallel",
                "yudao.ai.web-search.api-key=test-key");
    }

    @Test
    void testWebSearchDisabled() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(AiWebSearchClient.class));
        contextRunner.withPropertyValues("yudao.ai.web-search.enable=false", "yudao.ai.web-search.platform=parallel")
                .run(context -> assertThat(context).doesNotHaveBean(AiWebSearchClient.class));
    }

    private void assertProvider(Class<? extends AiWebSearchClient> provider, String... properties) {
        contextRunner.withPropertyValues("yudao.ai.web-search.enable=true").withPropertyValues(properties)
                .run(context -> assertThat(context).hasSingleBean(provider));
    }

}
