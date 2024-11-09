package cn.iocoder.yudao.module.iot.dal.dataobject.tdengine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * TD 引擎的数据库
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TdTableDO {

    /**
     * 数据库名称
     */
    private String dataBaseName;

    // TODO @haohao：superTableName 和 tableName 是不是合并。因为每个 mapper 操作的时候，有且只会使用到其中一个。
    /**
     * 超级表名称
     */
    private String superTableName;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * COLUMN 字段
     */
    private TdFieldDO column;

    /**
     * TAG 字段
     */
    private TdFieldDO tag;

    /**
     * COLUMN 字段 - 列表
     */
    private List<TdFieldDO> columns;

    /**
     * TAG 字段 - 列表
     */
    private List<TdFieldDO> tags;

    public TdTableDO(String dataBaseName, String superTableName, List<TdFieldDO> columns, List<TdFieldDO> tags) {
        this.dataBaseName = dataBaseName;
        this.superTableName = superTableName;
        this.columns = columns;
        this.tags = tags;
    }

    public TdTableDO(String dataBaseName, String superTableName) {
        this.dataBaseName = dataBaseName;
        this.superTableName = superTableName;
    }
}
