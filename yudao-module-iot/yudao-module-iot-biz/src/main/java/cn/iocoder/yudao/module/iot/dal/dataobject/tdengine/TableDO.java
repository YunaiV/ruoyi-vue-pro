package cn.iocoder.yudao.module.iot.dal.dataobject.tdengine;

import cn.iocoder.yudao.module.iot.domain.BaseEntity;
import lombok.Data;

import java.util.List;

@Data
public class TableDO extends BaseEntity {

    /**
     * 超级表普通列字段的值
     * 值需要与创建超级表时普通列字段的数据类型对应上
     */
    private List<TdFieldDO> schemaFieldValues;

    /**
     * 超级表标签字段的值
     * 值需要与创建超级表时标签字段的数据类型对应上
     */
    private List<TdFieldDO> tagsFieldValues;

    /**
     * 表名称
     */
    private String tableName;
}
