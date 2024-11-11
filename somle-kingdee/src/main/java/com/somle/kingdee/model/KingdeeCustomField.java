package com.somle.kingdee.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;


@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeeCustomField {

    private String baseEntityNumber;   // 基础资料标识，字段类型为5的时候独有该字段
    private List<Item> comboItems;     // 下拉选项，字段类型为4的时候独有该字段
    private String defValue;           // 默认值，字段类型为4的时候没有该字段
    private String displayName;        // 字段展示名称
    private int fieldType;             // 字段类型，（1：文本，2：数字，3：日期，4：辅助资料，5：基础资料，6：引用基础资料属性）
    private String id;                 // id
    private boolean mustInput;         // 必录
    private String number;             // 字段名

    @Data
    class Item {

        private String name;               // 下拉显示值
        private String value;              // 下拉后台传输值
    }
}