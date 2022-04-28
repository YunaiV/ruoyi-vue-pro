package cn.iocoder.yudao.module.infra.dal.dataobject.db;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

/**
 * MySQL 数据库中的 column 字段定义
 *
 * @author 芋道源码
 */
@Data
@Builder
public class DatabaseColumnDO {

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
    private String dataType;
    /**
     * 字段描述
     */
    private String columnComment;
    /**
     * 是否允许为空
     */
    private Boolean nullable;
    /**
     * 是否主键
     */
    private Boolean primaryKey;
    /**
     * 是否自增
     */
    private Boolean autoIncrement;
    /**
     * 排序字段
     */
    private Integer ordinalPosition;

}
