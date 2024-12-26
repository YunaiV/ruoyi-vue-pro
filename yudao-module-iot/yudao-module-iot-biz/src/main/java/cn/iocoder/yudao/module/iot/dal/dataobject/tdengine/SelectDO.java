package cn.iocoder.yudao.module.iot.dal.dataobject.tdengine;

import lombok.Data;

// TODO @haohao：类似这个，其实可以参考 mybatis plus，querywrapper，搞个 TdEngineQueryWrapper。这样看起来会更好懂。
/**
 * 查询DO
 */
@Data
@Deprecated
public class SelectDO {

    // TODO @haoha：database 是个单词
    /**
     * 数据库名称
     */
    private String dataBaseName;

    /**
     * 超级表名称
     */
    private String tableName;

    /**
     * 查询字段
     */
    private String fieldName;

    /**
     * 开始时间
     */
    private Long startTime;

    /**
     * 结束时间
     */
    private Long endTime;

    /**
     * 查询类型
     */
    private String type;

    /**
     * 设备ID
     */
    private String deviceId;
}
