package cn.iocoder.yudao.module.iot.dal.dataobject.tdengine;

import lombok.Data;

import java.util.List;

/**
 * TableData 类用于存储和操作 TDengine 表数据
 */
@Data
public class TableData {

    /**
     * 超级表普通列字段的名称
     */
    private List<String> schemaFieldList;

    /**
     * 超级表标签字段的值
     * 值需要与创建超级表时标签字段的数据类型对应上
     */
    private List<Object> tagsValueList;

    /**
     * 超级表普通列字段的值
     * 值需要与创建超级表时普通列字段的数据类型对应上
     */
    private List<Object> schemaValueList;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 超级表名称
     */
    private String superTableName;
}