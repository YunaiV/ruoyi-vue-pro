package cn.iocoder.yudao.module.iot.domain;

import lombok.Data;

@Data
public class BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 数据库名称
     */
    private String dataBaseName;

    /**
     * 超级表名称
     */
    private String superTableName;
}
