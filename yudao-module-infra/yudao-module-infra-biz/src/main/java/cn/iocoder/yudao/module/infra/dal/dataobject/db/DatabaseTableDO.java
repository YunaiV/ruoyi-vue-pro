package cn.iocoder.yudao.module.infra.dal.dataobject.db;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * MySQL 数据库中的 table 表定义
 *
 * @author 芋道源码
 */
@Data
@Builder
public class DatabaseTableDO {

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
