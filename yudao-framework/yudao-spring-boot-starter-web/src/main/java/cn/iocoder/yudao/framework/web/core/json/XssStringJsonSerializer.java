package cn.iocoder.yudao.framework.web.core.json;

import cn.iocoder.yudao.framework.web.config.XssProperties;
import cn.iocoder.yudao.framework.web.core.clean.XssCleaner;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AllArgsConstructor;

import java.io.IOException;

/**
 * XSS过滤 jackson 序列化器
 *
 * 参考 ballcat 实现
 */
@AllArgsConstructor
public class XssStringJsonSerializer extends JsonSerializer<String> {

    private final XssCleaner xssCleaner;
    private final XssProperties xssProperties;


    @Override
    public Class<String> handledType() {
        return String.class;
    }

    @Override
    public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        if (value != null) {
            // 开启 Xss 才进行处理
            if (xssProperties.isEnable()) {
                value = xssCleaner.clean(value);
            }
            jsonGenerator.writeString(value);
        }
    }

}

