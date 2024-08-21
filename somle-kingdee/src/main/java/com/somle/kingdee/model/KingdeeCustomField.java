package com.somle.kingdee.model;

import java.util.List;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.PropertyNamingStrategy;

import lombok.Data;

@Data
@JSONType(naming = PropertyNamingStrategy.SnakeCase)
public class KingdeeCustomField {
    private String number;
    private String displayName;
    private int fieldType;
    private boolean mustInput;
    private String defValue;
    private String baseEntityNumber;
    private List<String> comboItems;
    private String id;
}
