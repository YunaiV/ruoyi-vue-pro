package cn.iocoder.yudao.module.iot.domain;

import lombok.Data;

/**
 * @ClassDescription: tdEngine的基础实体类
 * @ClassName: BaseEntity
 * @Author: fxlinks
 * @Date: 2021-12-30 14:39:25
 * @Version 1.0
 */
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
