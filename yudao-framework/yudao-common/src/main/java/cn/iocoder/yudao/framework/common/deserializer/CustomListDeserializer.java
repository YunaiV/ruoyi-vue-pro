package cn.iocoder.yudao.framework.common.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @className: CustomListDeserializer
 * @author: Wqh
 * @date: 2024/10/30 10:46
 * @Version: 1.0
 * @description:
 */
public class CustomListDeserializer extends JsonDeserializer<List> {

    @Override
    public List deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        if (text == null || text.isEmpty() || "[]".equals(text)) {
            return new ArrayList<>();
        }
        return ctxt.readValue(p, List.class);
    }
}