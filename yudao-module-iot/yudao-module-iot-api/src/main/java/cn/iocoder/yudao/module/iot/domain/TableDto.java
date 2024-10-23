package cn.iocoder.yudao.module.iot.domain;

import lombok.Data;

import java.util.List;

/**
 * @ClassDescription: 创建超级表的子表需要的入参的实体类
 * @ClassName: TableDto
 * @Author: fxlinks
 * @Date: 2021-12-30 14:42:47
 * @Version 1.0
 */
@Data
public class TableDto extends BaseEntity {

    /**
     * 超级表普通列字段的值
     * 值需要与创建超级表时普通列字段的数据类型对应上
     */
    private List<Fields> schemaFieldValues;

    /**
     * 超级表标签字段的值
     * 值需要与创建超级表时标签字段的数据类型对应上
     */
    private List<Fields> tagsFieldValues;

    /**
     * 表名称
     */
    private String tableName;
}
