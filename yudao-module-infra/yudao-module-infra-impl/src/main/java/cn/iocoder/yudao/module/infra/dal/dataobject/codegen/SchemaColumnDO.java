package cn.iocoder.yudao.module.infra.dal.dataobject.codegen;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

/**
 * MySQL 数据库中的 column 字段定义
 *
 * @author 芋道源码
 */
@TableName(value = "information_schema.columns", autoResultMap = true)
@Data
@Builder
public class SchemaColumnDO {

    /**
     * 表名称
     */
    private String tableName;
    /**
     * 字段名
     */
    private String columnName;
    /**
     * 字段类型
     */
    private String columnType;
    /**
     * 字段描述
     */
    private String columnComment;
    /**
     * 是否允许为空
     */
    @TableField("case when is_nullable = 'yes' then '1' else '0' end")
    private Boolean nullable;
    /**
     * 是否主键
     */
    @TableField("case when column_key = 'PRI' then '1' else '0' end")
    private Boolean primaryKey;
    /**
     * 是否自增
     */
    @TableField("case when extra = 'auto_increment' then '1' else '0' end")
    private Boolean autoIncrement;
    /**
     * 排序字段
     */
    private Integer ordinalPosition;

}
