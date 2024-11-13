package com.somle.kingdee.model;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeeAuxInfoTypeDetailReqVO {
    private String createEndTime;      // 创建时间-结束时间的时间戳(毫秒)
    private String createStartTime;    // 创建时间-开始时间的时间戳(毫秒)
    private String modifyEndTime;      // 修改时间-结束时间的时间戳(毫秒)
    private String modifyStartTime;    // 修改时间-开始时间的时间戳(毫秒)
    private String page;               // 当前页，默认1
    private String pageSize;           // 每页显示条数默认10
    private List<String> parent;       // 上级分类id
    private String search;             // 按名称、编码模糊查询
}
