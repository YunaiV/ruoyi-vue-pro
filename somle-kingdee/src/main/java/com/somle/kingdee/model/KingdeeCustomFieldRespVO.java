package com.somle.kingdee.model;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;


@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeeCustomFieldRespVO {

    private List<KingdeeCustomField> head;                    // 头部字段（固定的参数名称，非分录字段）
    private List<BillEntry> instantPayFromToEntry;     // 实时付款条目
    private List<KingdeeCustomField> materialEntity;          // 分录字段（动态的参数名称，跟参数值配置的类型有关）
    private List<BillEntry> payFromToEntry;            // 付款条目

    @Data
    class BillEntry {

        private String entityNumber;       // 单据或基础资料类型，参考<基础资料及业务单据类型>
        private boolean auditLock;         // 审核锁定
        private String baseEntityNumber;   // 基础资料标识，字段类型为5的时候独有该字段
        private String defValue;           // 默认值，字段类型为4的时候没有该字段
        private String displayName;        // 字段展示名称
        private int fieldType;             // 字段类型，（1：文本，2：数字，3：日期，4：辅助资料，5：基础资料，6：引用基础资料属性）
        private String id;                 // id
        private boolean mustInput;         // 必录
        private String number;             // 字段名
        private String typeId;             // 单据或基础资料类型，参考<基础资料及业务单据类型>
    }



}
