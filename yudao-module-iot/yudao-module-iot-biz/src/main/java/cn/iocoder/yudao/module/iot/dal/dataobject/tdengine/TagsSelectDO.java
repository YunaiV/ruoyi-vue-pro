package cn.iocoder.yudao.module.iot.dal.dataobject.tdengine;

import lombok.Data;

/**
 * tags查询DO
 */
@Data
public class TagsSelectDO {

    /**
     * 数据库名称
     */
    private String dataBaseName;

    /**
     * 超级表名称
     */
    private String stableName;

    /**
     * tags名称
     */
    private String tagsName;

    /**
     * tags值
     */
    private Long startTime;

    /**
     * tags值
     */
    private Long endTime;

}
