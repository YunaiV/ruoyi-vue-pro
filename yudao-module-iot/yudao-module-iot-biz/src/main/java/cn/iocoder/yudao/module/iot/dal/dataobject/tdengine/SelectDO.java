package cn.iocoder.yudao.module.iot.dal.dataobject.tdengine;

import lombok.Data;

import java.util.Set;
/**
 * 查询DO
 */
@Data
public class SelectDO {

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
     * 查询条件
     */
    private Set<Integer> orgIds;

    /**
     * 设备ID
     */
    private String deviceId;
}
