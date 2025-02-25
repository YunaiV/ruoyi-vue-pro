package com.somle.ai.model;

import java.util.List;
import java.util.stream.Stream;


import com.fasterxml.jackson.databind.node.ObjectNode;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AiResponse {
    private String previous;
    private String next;
    private Integer count;
    private List<ObjectNode> results;

    public <T> Stream<T> getResults(Class<T> objectClass) {
        return results.stream().map(n-> JsonUtilsX.parseObject(n, objectClass));
    }
}
