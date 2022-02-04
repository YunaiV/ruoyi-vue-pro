package cn.iocoder.yudao.module.tool.dal.dataobject.codegen;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * MySQL 数据库中的 table 表定义
 *
 * @author 芋道源码
 */
@TableName(value = "information_schema.tables", autoResultMap = true)
@Data
@Builder
public class SchemaTableDO {

    /**
     * 数据库
     */
    private String tableSchema;
    /**
     * 表名称
     */
    private String tableName;
    /**
     * 表描述
     */
    private String tableComment;
    /**
     * 创建时间
     */
    private Date createTime;

}
