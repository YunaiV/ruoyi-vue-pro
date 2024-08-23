package com.somle.kingdee.model;

import java.time.LocalDateTime;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeeUnit {
    private int conversionType;
    private LocalDateTime createTime;
    private String creatorId;
    private boolean enable;
    private String id;
    private boolean isLeaf;
    private int level;
    private String longNumber;
    private String modifierId;
    private LocalDateTime modifyTime;
    private String name;
    private String number;
    private int precision;
    private int precisionAccount;
}
