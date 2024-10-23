package cn.iocoder.yudao.module.iot.domain.visual;

import lombok.Data;

import java.util.Map;

/**
 * @ClassDescription: 查询可视化所需入参对象
 * @ClassName: SelectDto
 * @Author: andyz
 * @Date: 2022-07-29 14:12:26
 * @Version 1.0
 */
@Data
public class SelectVisualDto {

//    @NotBlank(message = "invalid operation: tableName can not be empty")
    private String dataBaseName;

//    @NotBlank(message = "invalid operation: tableName can not be empty")
    private String tableName;

//    @NotBlank(message = "invalid operation: fieldName can not be empty") //属性
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
    //    @NotNull(message = "invalid operation: startTime can not be null")
    private Long startTime;

    //    @NotNull(message = "invalid operation: endTime can not be null")
    private Long endTime;

    /**
     * 请求参数
     */
    private Map<String, Object> params;

    private String sql;

    private String deviceId;

    private Long lastTime;
}
