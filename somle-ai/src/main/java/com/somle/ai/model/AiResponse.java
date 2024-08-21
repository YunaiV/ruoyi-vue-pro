package com.somle.ai.model;

import java.util.List;
import java.util.stream.Stream;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.annotation.JSONType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JSONType(naming = PropertyNamingStrategy.SnakeCase)
public class AiResponse {
    private String previous;
    private String next;
    private Integer count;
    private List<String> results;

    public <T> Stream<T> getResults(Class<T> objectClass) {
        return results.stream().map(n->JSON.parseObject(n, objectClass));
    }
}
