package cn.iocoder.dashboard.framework.jackson.config;

import cn.iocoder.dashboard.framework.jackson.ser.LongSerializer;
import cn.iocoder.dashboard.util.json.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    @SuppressWarnings("InstantiationOfUtilityClass")
    public JsonUtils jsonUtils(ObjectMapper objectMapper) {
        SimpleModule simpleModule = new SimpleModule();
        /*
         * 新增Long类型序列化规则，数值超过2^53-1，在JS会出现精度丢失问题，因此Long自动序列化为字符串类型
          */
        simpleModule.addSerializer(Long.class,LongSerializer.getInstance())
                    .addSerializer(Long.TYPE,LongSerializer.getInstance());
        objectMapper.registerModule(simpleModule);

        JsonUtils.init(objectMapper);
        return new JsonUtils();
    }
}
