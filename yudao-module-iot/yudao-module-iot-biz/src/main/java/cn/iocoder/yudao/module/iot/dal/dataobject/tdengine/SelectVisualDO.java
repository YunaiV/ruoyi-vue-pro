package cn.iocoder.yudao.module.iot.dal.dataobject.tdengine;

import lombok.Data;

import java.util.Map;

// TODO @haohao：类似 SelectDO 的想法，只是它是返回。ps：貌似可以在 tdengine 里面，创建一个 query 包，放这种比较特殊的查询和结果对象。dataobject 更多还是实际存储的结构化的 do
@Data
public class SelectVisualDO {

    /**
     * 数据库名称
     */
    private String dataBaseName;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 属性
     */
    private String fieldName;

    /**
     * 查询类型，0历史数据，1实时数据，2聚合数据
     */
    private int type;

    /**
     * 查询的数据量
     */
    private int num;

    /**
     * 聚合函数
     */
    private String aggregate;

    /**
     * 统计间隔数字+s/m/h/d
     * 比如1s,1m,1h,1d代表1秒，1分钟，1小时，1天
     */
    private String interval;

    /**
     * 开始时间
     */
    private Long startTime;

    /**
     * 结束时间
     */
    private Long endTime;

    /**
     * 请求参数
     */
    private Map<String, Object> params;

    private String sql;

    private String deviceId;

    private Long lastTime;
}
