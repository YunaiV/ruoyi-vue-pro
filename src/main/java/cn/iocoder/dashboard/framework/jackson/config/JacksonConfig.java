package cn.iocoder.dashboard.framework.jackson.config;

import cn.iocoder.dashboard.util.json.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    @SuppressWarnings("InstantiationOfUtilityClass")
    public JsonUtils jsonUtils(ObjectMapper objectMapper) {
        JsonUtils.init(objectMapper);
        return new JsonUtils();
    }

}
